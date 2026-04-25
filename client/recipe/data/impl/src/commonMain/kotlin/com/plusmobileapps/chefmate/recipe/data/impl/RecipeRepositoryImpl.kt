package com.plusmobileapps.chefmate.recipe.data.impl

import app.cash.sqldelight.coroutines.asFlow
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.database.Recipe as DbRecipe
import com.plusmobileapps.chefmate.database.RecipeQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeCollaborator
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.plusmobileapps.chefmate.recipe.data.RecipeRole
import com.plusmobileapps.chefmate.recipe.data.SyncStatus
import com.plusmobileapps.chefmate.recipe.data.impl.remote.RecipeRemoteDataSource
import com.plusmobileapps.chefmate.recipe.data.impl.remote.RemoteRecipe
import com.plusmobileapps.chefmate.recipe.data.impl.remote.RemoteRecipeShare
import com.plusmobileapps.chefmate.util.DateTimeUtil
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@OptIn(ExperimentalUuidApi::class)
@SingleIn(AppScope::class)
@Inject
@ContributesBinding(AppScope::class)
class RecipeRepositoryImpl(
    private val db: RecipeQueries,
    @IO private val ioContext: CoroutineContext,
    private val dateTimeUtil: DateTimeUtil,
    private val remoteDataSource: RecipeRemoteDataSource,
    private val authRepository: AuthenticationRepository,
) : RecipeRepository {

    private val scope = CoroutineScope(ioContext + SupervisorJob())
    private val syncMutex = Mutex()
    private val syncingIds = MutableStateFlow<Set<Long>>(emptySet())

    init {
        scope.launch {
            authRepository.state.collect { state ->
                if (state is AuthState.Authenticated) {
                    syncWithRemote(state.user.userId)
                }
            }
        }
    }

    override fun getRecipes(): Flow<List<Recipe>> =
        combine(db.getAll().asFlow().map { it.executeAsList() }, syncingIds) { items, syncing ->
                items.map { it.toRecipe(syncing) }
            }
            .flowOn(ioContext)

    override suspend fun createRecipe(recipe: Recipe): Recipe {
        val clientId = Uuid.random().toString()
        val result =
            withContext(ioContext) {
                db.transactionWithResult {
                    db.create(
                        title = recipe.title,
                        description = recipe.description,
                        ingredients = recipe.ingredients,
                        directions = recipe.directions,
                        imageUrl = recipe.imageUrl,
                        sourceUrl = recipe.sourceUrl,
                        servings = recipe.servings?.toLong(),
                        prepTime = recipe.prepTime?.toLong(),
                        cookTime = recipe.cookTime?.toLong(),
                        totalTime = recipe.totalTime?.toLong(),
                        calories = recipe.calories?.toLong(),
                        starRating = recipe.starRating?.toLong(),
                        isFavorite = recipe.isFavorite,
                        createdAt = dateTimeUtil.now.toString(),
                        updatedAt = dateTimeUtil.now.toString(),
                        clientId = clientId,
                        ownerId = null,
                    )
                    val id =
                        db.lastInsertId().executeAsOne().MAX
                            ?: error("Failed to get last insert id")
                    db.getById(id).executeAsOne().toRecipe()
                }
            }
        pushAddToRemote(result.id)
        return result
    }

    override suspend fun updateRecipe(recipe: Recipe): Recipe {
        val result =
            withContext(ioContext) {
                val now = dateTimeUtil.now
                db.transactionWithResult {
                    db.update(
                        id = recipe.id,
                        title = recipe.title,
                        description = recipe.description,
                        ingredients = recipe.ingredients,
                        directions = recipe.directions,
                        imageUrl = recipe.imageUrl,
                        sourceUrl = recipe.sourceUrl,
                        servings = recipe.servings?.toLong(),
                        prepTime = recipe.prepTime?.toLong(),
                        cookTime = recipe.cookTime?.toLong(),
                        totalTime = recipe.totalTime?.toLong(),
                        calories = recipe.calories?.toLong(),
                        starRating = recipe.starRating?.toLong(),
                        isFavorite = recipe.isFavorite,
                        updatedAt = now.toString(),
                    )
                    recipe.copy(updatedAt = now)
                }
            }
        pushUpdateToRemote(recipe.id)
        return result
    }

    override suspend fun getRecipe(id: Long): Flow<Recipe?> =
        db.getById(id)
            .asFlow()
            .map { it.executeAsOneOrNull() }
            .map { it?.toRecipe() }
            .flowOn(ioContext)

    override suspend fun deleteRecipe(id: Long) {
        val entity =
            withContext(ioContext) {
                val entity = db.getById(id).executeAsOneOrNull()
                db.delete(id)
                entity
            }
        entity?.remoteId?.let { remoteId ->
            scope.launch {
                try {
                    remoteDataSource.deleteRecipe(remoteId)
                } catch (_: Exception) {}
            }
        }
    }

    override suspend fun clearLocalData() {
        withContext(ioContext) { db.deleteAll() }
    }

    override suspend fun syncAllUnsynced() {
        val authState = authRepository.state.value
        if (authState is AuthState.Authenticated) {
            syncWithRemote(authState.user.userId)
        }
    }

    private fun pushAddToRemote(localId: Long) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        scope.launch {
            try {
                val entity = db.getById(localId).executeAsOneOrNull() ?: return@launch
                val clientId =
                    entity.clientId
                        ?: Uuid.random().toString().also { newId ->
                            db.updateClientId(clientId = newId, id = localId)
                        }
                syncingIds.update { it + localId }
                try {
                    val remoteRecipe =
                        remoteDataSource.upsertRecipe(
                            RemoteRecipe(
                                ownerId = authState.user.userId,
                                title = entity.title,
                                description = entity.description,
                                ingredients = entity.ingredients,
                                directions = entity.directions,
                                imageUrl = entity.imageUrl,
                                sourceUrl = entity.sourceUrl,
                                servings = entity.servings?.toInt(),
                                prepTime = entity.prepTime?.toInt(),
                                cookTime = entity.cookTime?.toInt(),
                                totalTime = entity.totalTime?.toInt(),
                                calories = entity.calories?.toInt(),
                                starRating = entity.starRating?.toInt(),
                                isFavorite = entity.isFavorite,
                                createdAt = entity.createdAt,
                                updatedAt = entity.updatedAt,
                                clientId = clientId,
                            )
                        )
                    db.updateRemoteId(remoteId = remoteRecipe.id, id = localId)
                } finally {
                    syncingIds.update { it - localId }
                }
            } catch (_: Exception) {}
        }
    }

    private fun pushUpdateToRemote(localId: Long) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        scope.launch {
            try {
                val entity = db.getById(localId).executeAsOneOrNull() ?: return@launch
                val remoteId = entity.remoteId ?: return@launch
                syncingIds.update { it + localId }
                try {
                    remoteDataSource.upsertRecipe(
                        RemoteRecipe(
                            id = remoteId,
                            ownerId = authState.user.userId,
                            title = entity.title,
                            description = entity.description,
                            ingredients = entity.ingredients,
                            directions = entity.directions,
                            imageUrl = entity.imageUrl,
                            sourceUrl = entity.sourceUrl,
                            servings = entity.servings?.toInt(),
                            prepTime = entity.prepTime?.toInt(),
                            cookTime = entity.cookTime?.toInt(),
                            totalTime = entity.totalTime?.toInt(),
                            calories = entity.calories?.toInt(),
                            starRating = entity.starRating?.toInt(),
                            isFavorite = entity.isFavorite,
                            updatedAt = entity.updatedAt,
                            clientId = entity.clientId,
                        )
                    )
                    db.clearDirty(localId)
                } finally {
                    syncingIds.update { it - localId }
                }
            } catch (_: Exception) {}
        }
    }

    private suspend fun syncWithRemote(userId: String) = syncMutex.withLock {
        try {
            // Push unsynced recipes (no remoteId yet)
            val unsynced = withContext(ioContext) { db.getUnsynced().executeAsList() }
            for (recipe in unsynced) {
                try {
                    val clientId =
                        recipe.clientId
                            ?: Uuid.random().toString().also { newId ->
                                withContext(ioContext) {
                                    db.updateClientId(clientId = newId, id = recipe.id)
                                }
                            }
                    val remoteRecipe =
                        remoteDataSource.upsertRecipe(
                            RemoteRecipe(
                                ownerId = userId,
                                title = recipe.title,
                                description = recipe.description,
                                ingredients = recipe.ingredients,
                                directions = recipe.directions,
                                imageUrl = recipe.imageUrl,
                                sourceUrl = recipe.sourceUrl,
                                servings = recipe.servings?.toInt(),
                                prepTime = recipe.prepTime?.toInt(),
                                cookTime = recipe.cookTime?.toInt(),
                                totalTime = recipe.totalTime?.toInt(),
                                calories = recipe.calories?.toInt(),
                                starRating = recipe.starRating?.toInt(),
                                isFavorite = recipe.isFavorite,
                                createdAt = recipe.createdAt,
                                updatedAt = recipe.updatedAt,
                                clientId = clientId,
                            )
                        )
                    withContext(ioContext) {
                        db.updateRemoteId(remoteId = remoteRecipe.id, id = recipe.id)
                    }
                } catch (_: Exception) {}
            }

            // Push dirty recipes (modified locally, already have remoteId)
            val dirty = withContext(ioContext) { db.getDirty().executeAsList() }
            for (recipe in dirty) {
                try {
                    val remoteId = recipe.remoteId ?: continue
                    remoteDataSource.upsertRecipe(
                        RemoteRecipe(
                            id = remoteId,
                            ownerId = userId,
                            title = recipe.title,
                            description = recipe.description,
                            ingredients = recipe.ingredients,
                            directions = recipe.directions,
                            imageUrl = recipe.imageUrl,
                            sourceUrl = recipe.sourceUrl,
                            servings = recipe.servings?.toInt(),
                            prepTime = recipe.prepTime?.toInt(),
                            cookTime = recipe.cookTime?.toInt(),
                            totalTime = recipe.totalTime?.toInt(),
                            calories = recipe.calories?.toInt(),
                            starRating = recipe.starRating?.toInt(),
                            isFavorite = recipe.isFavorite,
                            updatedAt = recipe.updatedAt,
                            clientId = recipe.clientId,
                        )
                    )
                    withContext(ioContext) { db.clearDirty(recipe.id) }
                } catch (_: Exception) {}
            }

            // Pull all accessible recipes (RLS handles filtering)
            val remoteRecipes = remoteDataSource.fetchAccessibleRecipes()
            withContext(ioContext) {
                for (remote in remoteRecipes) {
                    val remoteId = remote.id ?: continue
                    val isOwned = remote.ownerId == userId
                    val role = if (isOwned) "owner" else "viewer"
                    val isShared = !isOwned

                    val existing = db.getByRemoteId(remoteId).executeAsOneOrNull()
                    if (existing != null) {
                        db.updateOwnership(role = role, isShared = isShared, id = existing.id)
                        if (remote.forkedFrom != null) {
                            db.updateForkedFrom(
                                forkedFromRemoteId = remote.forkedFrom,
                                forkedFromTitle = null,
                                id = existing.id,
                            )
                        }
                        continue
                    }

                    val matchedByClientId =
                        remote.clientId?.let { clientId ->
                            db.getByClientId(clientId).executeAsOneOrNull()
                        }
                    if (matchedByClientId != null) {
                        db.updateRemoteId(remoteId = remoteId, id = matchedByClientId.id)
                        db.updateOwnership(
                            role = role,
                            isShared = isShared,
                            id = matchedByClientId.id,
                        )
                    } else {
                        db.createWithRemoteId(
                            title = remote.title,
                            description = remote.description,
                            ingredients = remote.ingredients,
                            directions = remote.directions,
                            imageUrl = remote.imageUrl,
                            sourceUrl = remote.sourceUrl,
                            servings = remote.servings?.toLong(),
                            prepTime = remote.prepTime?.toLong(),
                            cookTime = remote.cookTime?.toLong(),
                            totalTime = remote.totalTime?.toLong(),
                            calories = remote.calories?.toLong(),
                            starRating = remote.starRating?.toLong(),
                            isFavorite = remote.isFavorite,
                            createdAt = remote.createdAt ?: dateTimeUtil.now.toString(),
                            updatedAt = remote.updatedAt ?: dateTimeUtil.now.toString(),
                            remoteId = remoteId,
                            clientId = remote.clientId,
                            ownerId = remote.ownerId,
                        )
                        val newId = db.lastInsertId().executeAsOne().MAX
                        if (newId != null) {
                            db.updateOwnership(role = role, isShared = isShared, id = newId)
                            if (remote.forkedFrom != null) {
                                db.updateForkedFrom(
                                    forkedFromRemoteId = remote.forkedFrom,
                                    forkedFromTitle = null,
                                    id = newId,
                                )
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    override fun getSharedRecipes(): Flow<List<Recipe>> =
        db.getShared()
            .asFlow()
            .map { it.executeAsList() }
            .map { recipes -> recipes.map { it.toRecipe() } }
            .flowOn(ioContext)

    override suspend fun shareRecipe(recipeId: Long, email: String, role: RecipeRole) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        val recipe = withContext(ioContext) { db.getById(recipeId).executeAsOneOrNull() } ?: return
        val remoteRecipeId = recipe.remoteId ?: return

        remoteDataSource.shareRecipe(
            RemoteRecipeShare(
                recipeId = remoteRecipeId,
                invitedEmail = email,
                role = role.name.lowercase(),
                invitedBy = authState.user.userId,
            )
        )
    }

    override suspend fun forkRecipe(recipeId: Long): Recipe {
        val original =
            withContext(ioContext) { db.getById(recipeId).executeAsOneOrNull() }
                ?: error("Recipe not found")
        val forkedFromRemoteId = original.remoteId

        val forked =
            createRecipe(
                Recipe(
                    id = 0,
                    title = original.title,
                    description = original.description,
                    ingredients = original.ingredients.orEmpty(),
                    directions = original.directions.orEmpty(),
                    imageUrl = original.imageUrl,
                    sourceUrl = original.sourceUrl,
                    servings = original.servings?.toInt(),
                    prepTime = original.prepTime?.toInt(),
                    cookTime = original.cookTime?.toInt(),
                    totalTime = original.totalTime?.toInt(),
                    calories = original.calories?.toInt(),
                    starRating = original.starRating?.toInt(),
                    isFavorite = false,
                    createdAt = Instant.parse(dateTimeUtil.now.toString()),
                    updatedAt = Instant.parse(dateTimeUtil.now.toString()),
                    forkedFromRemoteId = forkedFromRemoteId,
                    forkedFromTitle = original.title,
                )
            )

        if (forkedFromRemoteId != null) {
            withContext(ioContext) {
                db.updateForkedFrom(
                    forkedFromRemoteId = forkedFromRemoteId,
                    forkedFromTitle = original.title,
                    id = forked.id,
                )
            }
        }

        return forked
    }

    override suspend fun acceptRecipeShare(recipeId: Long) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        val recipe = withContext(ioContext) { db.getById(recipeId).executeAsOneOrNull() } ?: return
        val remoteRecipeId = recipe.remoteId ?: return

        val shares = remoteDataSource.fetchRecipeShares(remoteRecipeId)
        val myShare = shares.firstOrNull { it.userId == authState.user.userId }
        myShare?.id?.let { remoteDataSource.respondToRecipeShare(it, accept = true) }
    }

    override suspend fun rejectRecipeShare(recipeId: Long) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        val recipe = withContext(ioContext) { db.getById(recipeId).executeAsOneOrNull() } ?: return
        val remoteRecipeId = recipe.remoteId ?: return

        val shares = remoteDataSource.fetchRecipeShares(remoteRecipeId)
        val myShare = shares.firstOrNull { it.userId == authState.user.userId }
        myShare?.id?.let { remoteDataSource.respondToRecipeShare(it, accept = false) }
        withContext(ioContext) { db.delete(recipeId) }
    }

    override fun getRecipeCollaborators(recipeId: Long): Flow<List<RecipeCollaborator>> =
        MutableStateFlow(emptyList<RecipeCollaborator>())

    private fun String.toRecipeRole(): RecipeRole =
        when (this) {
            "editor" -> RecipeRole.EDITOR
            "viewer" -> RecipeRole.VIEWER
            else -> RecipeRole.OWNER
        }

    private fun DbRecipe.toRecipe(syncing: Set<Long> = emptySet()): Recipe {
        val syncStatus =
            when {
                id in syncing -> SyncStatus.SYNCING
                isDirty -> SyncStatus.NOT_SYNCED
                remoteId != null -> SyncStatus.SYNCED
                else -> SyncStatus.NOT_SYNCED
            }
        return Recipe(
            id = id,
            title = title,
            description = description,
            ingredients = ingredients.orEmpty(),
            directions = directions.orEmpty(),
            imageUrl = imageUrl,
            sourceUrl = sourceUrl,
            servings = servings?.toInt(),
            prepTime = prepTime?.toInt(),
            cookTime = cookTime?.toInt(),
            totalTime = totalTime?.toInt(),
            calories = calories?.toInt(),
            starRating = starRating?.toInt(),
            isFavorite = isFavorite,
            syncStatus = syncStatus,
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt),
            forkedFromRemoteId = forkedFromRemoteId,
            forkedFromTitle = forkedFromTitle,
            role = role.toRecipeRole(),
            isShared = isShared,
        )
    }
}
