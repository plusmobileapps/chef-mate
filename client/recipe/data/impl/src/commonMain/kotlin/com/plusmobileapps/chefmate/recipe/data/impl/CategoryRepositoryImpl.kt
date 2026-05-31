package com.plusmobileapps.chefmate.recipe.data.impl

import app.cash.sqldelight.coroutines.asFlow
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.database.Category as DbCategory
import com.plusmobileapps.chefmate.database.CategoryQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.CategoryRepository
import com.plusmobileapps.chefmate.recipe.data.SyncStatus
import com.plusmobileapps.chefmate.recipe.data.impl.remote.CategoryRemoteDataSource
import com.plusmobileapps.chefmate.recipe.data.impl.remote.RemoteCategory
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@OptIn(ExperimentalUuidApi::class)
@SingleIn(AppScope::class)
@Inject
@ContributesBinding(AppScope::class)
class CategoryRepositoryImpl(
    private val db: CategoryQueries,
    @IO private val ioContext: CoroutineContext,
    private val remoteDataSource: CategoryRemoteDataSource,
    private val authRepository: AuthenticationRepository,
) : CategoryRepository {

    private val scope = CoroutineScope(ioContext + SupervisorJob())
    private val syncMutex = Mutex()
    // Materialization is a find-or-insert; serialized to keep two simultaneous attaches of the
    // same preset from racing past the partial unique index.
    private val materializeMutex = Mutex()

    init {
        scope.launch {
            authRepository.state.collect { state ->
                if (state is AuthState.Authenticated) {
                    syncWithRemote(state.user.userId)
                }
            }
        }
    }

    override fun observeUserCategories(): Flow<List<Category>> =
        db.getAll()
            .asFlow()
            .map { it.executeAsList().map { row -> row.toCategory() } }
            .flowOn(ioContext)

    override suspend fun findBuiltin(builtin: BuiltinCategory): Category? {
        val ownerId = authRepository.state.value.userIdOrNull()
        return withContext(ioContext) {
            db.getBuiltinForOwner(ownerId = ownerId, builtinId = builtin.id)
                .executeAsOneOrNull()
                ?.toCategory()
        }
    }

    override suspend fun materializeBuiltin(builtin: BuiltinCategory): Category =
        materializeMutex.withLock {
            findBuiltin(builtin)?.let {
                return@withLock it
            }
            val ownerId = authRepository.state.value.userIdOrNull()
            val clientId = Uuid.random().toString()
            val created =
                withContext(ioContext) {
                    db.transactionWithResult {
                        db.create(
                            name = builtin.canonicalName(),
                            builtinId = builtin.id,
                            clientId = clientId,
                            ownerId = ownerId,
                        )
                        val id =
                            db.lastInsertId().executeAsOne().MAX
                                ?: error("Failed to get last insert id")
                        db.getById(id).executeAsOne().toCategory()
                    }
                }
            pushAddToRemote(created.id)
            created
        }

    override suspend fun createUserCategory(name: String): Category {
        val ownerId = authRepository.state.value.userIdOrNull()
        val clientId = Uuid.random().toString()
        val created =
            withContext(ioContext) {
                db.transactionWithResult {
                    db.create(name = name, builtinId = null, clientId = clientId, ownerId = ownerId)
                    val id =
                        db.lastInsertId().executeAsOne().MAX
                            ?: error("Failed to get last insert id")
                    db.getById(id).executeAsOne().toCategory()
                }
            }
        pushAddToRemote(created.id)
        return created
    }

    override suspend fun renameCategory(id: Long, name: String): Category {
        val updated =
            withContext(ioContext) {
                db.transactionWithResult {
                    db.rename(name = name, id = id)
                    db.getById(id).executeAsOne().toCategory()
                }
            }
        pushUpdateToRemote(id)
        return updated
    }

    override suspend fun deleteCategory(id: Long) {
        val entity =
            withContext(ioContext) {
                val entity = db.getById(id).executeAsOneOrNull()
                db.delete(id)
                entity
            }
        entity?.remoteId?.let { remoteId ->
            scope.launch {
                try {
                    remoteDataSource.deleteCategory(remoteId)
                } catch (_: Exception) {}
            }
        }
    }

    override suspend fun clearLocalData() {
        withContext(ioContext) { db.deleteAll() }
    }

    private fun pushAddToRemote(localId: Long) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        scope.launch {
            try {
                val entity =
                    withContext(ioContext) { db.getById(localId).executeAsOneOrNull() }
                        ?: return@launch
                val clientId =
                    entity.clientId
                        ?: Uuid.random().toString().also { newId ->
                            withContext(ioContext) {
                                db.updateClientId(clientId = newId, id = localId)
                            }
                        }
                val remote =
                    remoteDataSource.upsertCategory(
                        RemoteCategory(
                            ownerId = authState.user.userId,
                            name = entity.name,
                            builtinId = entity.builtinId,
                            clientId = clientId,
                        )
                    )
                withContext(ioContext) { db.updateRemoteId(remoteId = remote.id, id = localId) }
            } catch (_: Exception) {}
        }
    }

    private fun pushUpdateToRemote(localId: Long) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        scope.launch {
            try {
                val entity =
                    withContext(ioContext) { db.getById(localId).executeAsOneOrNull() }
                        ?: return@launch
                val remoteId = entity.remoteId ?: return@launch
                remoteDataSource.upsertCategory(
                    RemoteCategory(
                        id = remoteId,
                        ownerId = authState.user.userId,
                        name = entity.name,
                        builtinId = entity.builtinId,
                        clientId = entity.clientId,
                    )
                )
                withContext(ioContext) { db.clearDirty(localId) }
            } catch (_: Exception) {}
        }
    }

    private suspend fun syncWithRemote(userId: String) = syncMutex.withLock {
        try {
            // Push unsynced categories (no remoteId yet)
            val unsynced = withContext(ioContext) { db.getUnsynced().executeAsList() }
            for (cat in unsynced) {
                try {
                    val clientId =
                        cat.clientId
                            ?: Uuid.random().toString().also { newId ->
                                withContext(ioContext) {
                                    db.updateClientId(clientId = newId, id = cat.id)
                                }
                            }
                    val remote =
                        remoteDataSource.upsertCategory(
                            RemoteCategory(
                                ownerId = userId,
                                name = cat.name,
                                builtinId = cat.builtinId,
                                clientId = clientId,
                            )
                        )
                    withContext(ioContext) { db.updateRemoteId(remoteId = remote.id, id = cat.id) }
                } catch (_: Exception) {}
            }

            // Push dirty categories (modified locally, already have remoteId)
            val dirty = withContext(ioContext) { db.getDirty().executeAsList() }
            for (cat in dirty) {
                try {
                    val remoteId = cat.remoteId ?: continue
                    remoteDataSource.upsertCategory(
                        RemoteCategory(
                            id = remoteId,
                            ownerId = userId,
                            name = cat.name,
                            builtinId = cat.builtinId,
                            clientId = cat.clientId,
                        )
                    )
                    withContext(ioContext) { db.clearDirty(cat.id) }
                } catch (_: Exception) {}
            }

            // Pull remote categories not yet local
            val remoteCats = remoteDataSource.fetchAllCategories(userId)
            withContext(ioContext) {
                for (remote in remoteCats) {
                    val remoteId = remote.id ?: continue
                    if (db.getByRemoteId(remoteId).executeAsOneOrNull() != null) continue
                    val matchedByClientId =
                        remote.clientId?.let { id -> db.getByClientId(id).executeAsOneOrNull() }
                    if (matchedByClientId != null) {
                        db.updateRemoteId(remoteId = remoteId, id = matchedByClientId.id)
                    } else {
                        db.createWithRemoteId(
                            name = remote.name,
                            builtinId = remote.builtinId,
                            remoteId = remoteId,
                            clientId = remote.clientId,
                            ownerId = userId,
                        )
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun DbCategory.toCategory(): Category =
        Category(
            id = id,
            name = name,
            builtinId = builtinId,
            syncStatus =
                when {
                    isDirty -> SyncStatus.NOT_SYNCED
                    remoteId != null -> SyncStatus.SYNCED
                    else -> SyncStatus.NOT_SYNCED
                },
        )

    private fun AuthState.userIdOrNull(): String? = (this as? AuthState.Authenticated)?.user?.userId

    /**
     * The English-language fallback name stored on the row. UI should resolve presets via
     * `BuiltinCategory.fromId(builtinId)` and look up the localized label from `strings.xml`; this
     * name is only used if the builtinId is unknown to the running build.
     */
    private fun BuiltinCategory.canonicalName(): String =
        when (this) {
            BuiltinCategory.BREAKFAST -> "Breakfast"
            BuiltinCategory.LUNCH -> "Lunch"
            BuiltinCategory.DINNER -> "Dinner"
            BuiltinCategory.APPETIZER -> "Appetizer"
            BuiltinCategory.SIDE -> "Side"
            BuiltinCategory.DESSERT -> "Dessert"
            BuiltinCategory.SNACK -> "Snack"
            BuiltinCategory.DRINK -> "Drink"
            BuiltinCategory.OTHER -> "Other"
            BuiltinCategory.AI -> "AI"
        }
}
