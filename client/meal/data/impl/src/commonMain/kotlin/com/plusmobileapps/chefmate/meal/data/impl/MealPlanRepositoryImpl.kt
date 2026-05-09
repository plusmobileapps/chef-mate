package com.plusmobileapps.chefmate.meal.data.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.database.MealPlanQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import com.plusmobileapps.chefmate.meal.data.MealPlanRepository
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.meal.data.SyncStatus
import com.plusmobileapps.chefmate.meal.data.impl.remote.MealPlanRemoteDataSource
import com.plusmobileapps.chefmate.meal.data.impl.remote.RemoteMealPlan
import com.plusmobileapps.chefmate.util.DateTimeUtil
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@OptIn(ExperimentalUuidApi::class)
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class MealPlanRepositoryImpl(
    private val queries: MealPlanQueries,
    @IO private val ioContext: CoroutineContext,
    private val dateTimeUtil: DateTimeUtil,
    private val remoteDataSource: MealPlanRemoteDataSource,
    private val authRepository: AuthenticationRepository,
) : MealPlanRepository {

    private val scope = CoroutineScope(ioContext + SupervisorJob())
    private val syncMutex = Mutex()

    init {
        scope.launch {
            authRepository.state.collect { state ->
                if (state is AuthState.Authenticated) {
                    syncWithRemote(state.user.userId)
                }
            }
        }
    }

    override fun getMealsByDate(date: String): Flow<List<MealPlanItem>> =
        queries.getByDate(date).asFlow().mapToList(ioContext).map { rows ->
            rows.map { it.toMealPlanItem() }
        }

    override fun getMealsByDateRange(startDate: String, endDate: String): Flow<List<MealPlanItem>> =
        queries.getByDateRange(startDate, endDate).asFlow().mapToList(ioContext).map { rows ->
            rows.map { it.toMealPlanItem() }
        }

    override suspend fun addMeal(recipeId: Long, date: String, mealType: MealType) {
        val clientId = Uuid.random().toString()
        val now = dateTimeUtil.now.toString()
        val localId =
            withContext(ioContext) {
                queries.transactionWithResult {
                    queries.insert(
                        recipeId = recipeId,
                        date = date,
                        mealType = mealType.name,
                        clientId = clientId,
                        createdAt = now,
                        updatedAt = now,
                    )
                    queries.lastInsertId().executeAsOne().MAX
                        ?: error("Failed to get last insert id")
                }
            }
        pushAddToRemote(localId)
    }

    override suspend fun removeMeal(id: Long) {
        val entity =
            withContext(ioContext) {
                val entity = queries.getById(id).executeAsOneOrNull()
                queries.delete(id)
                entity
            }
        entity?.remoteId?.let { remoteId ->
            scope.launch {
                try {
                    remoteDataSource.deleteMealPlan(remoteId)
                } catch (_: Exception) {}
            }
        }
    }

    override suspend fun syncAllUnsynced() {
        val authState = authRepository.state.value
        if (authState is AuthState.Authenticated) {
            syncWithRemote(authState.user.userId)
        }
    }

    override suspend fun clearLocalData() {
        withContext(ioContext) { queries.deleteAll() }
    }

    private fun pushAddToRemote(localId: Long) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        scope.launch {
            try {
                val entity =
                    withContext(ioContext) { queries.getById(localId).executeAsOneOrNull() }
                        ?: return@launch
                val clientId =
                    entity.clientId
                        ?: Uuid.random().toString().also { newId ->
                            withContext(ioContext) {
                                queries.updateClientId(clientId = newId, id = localId)
                            }
                        }
                val remote =
                    remoteDataSource.upsertMealPlan(
                        RemoteMealPlan(
                            ownerId = authState.user.userId,
                            recipeId = entity.recipeId,
                            date = entity.date,
                            mealType = entity.mealType,
                            createdAt = entity.createdAt,
                            updatedAt = entity.updatedAt,
                            clientId = clientId,
                        )
                    )
                withContext(ioContext) {
                    queries.updateRemoteId(remoteId = remote.id, id = localId)
                }
            } catch (_: Exception) {}
        }
    }

    private suspend fun syncWithRemote(userId: String) = syncMutex.withLock {
        try {
            // Push unsynced meal plans (no remoteId yet)
            val unsynced = withContext(ioContext) { queries.getUnsynced().executeAsList() }
            for (meal in unsynced) {
                try {
                    val clientId =
                        meal.clientId
                            ?: Uuid.random().toString().also { newId ->
                                withContext(ioContext) {
                                    queries.updateClientId(clientId = newId, id = meal.id)
                                }
                            }
                    val remote =
                        remoteDataSource.upsertMealPlan(
                            RemoteMealPlan(
                                ownerId = userId,
                                recipeId = meal.recipeId,
                                date = meal.date,
                                mealType = meal.mealType,
                                createdAt = meal.createdAt,
                                updatedAt = meal.updatedAt,
                                clientId = clientId,
                            )
                        )
                    withContext(ioContext) {
                        queries.updateRemoteId(remoteId = remote.id, id = meal.id)
                    }
                } catch (_: Exception) {}
            }

            // Pull remote meal plans
            val remoteMeals = remoteDataSource.fetchAllMealPlans(userId)
            withContext(ioContext) {
                for (remote in remoteMeals) {
                    val remoteId = remote.id ?: continue
                    val existing = queries.getByRemoteId(remoteId).executeAsOneOrNull()
                    if (existing != null) continue

                    val matchedByClientId =
                        remote.clientId?.let { clientId ->
                            queries.getByClientId(clientId).executeAsOneOrNull()
                        }
                    if (matchedByClientId != null) {
                        queries.updateRemoteId(remoteId = remoteId, id = matchedByClientId.id)
                    } else {
                        queries.insertWithRemoteId(
                            recipeId = remote.recipeId,
                            date = remote.date,
                            mealType = remote.mealType,
                            clientId = remote.clientId,
                            createdAt = remote.createdAt ?: dateTimeUtil.now.toString(),
                            updatedAt = remote.updatedAt ?: dateTimeUtil.now.toString(),
                            remoteId = remoteId,
                            ownerId = userId,
                        )
                    }
                }
            }
        } catch (_: Exception) {}
    }
}

private fun com.plusmobileapps.chefmate.database.GetByDate.toMealPlanItem(): MealPlanItem =
    MealPlanItem(
        id = id,
        recipeId = recipeId,
        recipeTitle = recipeTitle,
        recipeImageUrl = recipeImageUrl,
        date = date,
        mealType = MealType.valueOf(mealType),
        syncStatus = if (remoteId != null) SyncStatus.SYNCED else SyncStatus.NOT_SYNCED,
    )

private fun com.plusmobileapps.chefmate.database.GetByDateRange.toMealPlanItem(): MealPlanItem =
    MealPlanItem(
        id = id,
        recipeId = recipeId,
        recipeTitle = recipeTitle,
        recipeImageUrl = recipeImageUrl,
        date = date,
        mealType = MealType.valueOf(mealType),
        syncStatus = if (remoteId != null) SyncStatus.SYNCED else SyncStatus.NOT_SYNCED,
    )
