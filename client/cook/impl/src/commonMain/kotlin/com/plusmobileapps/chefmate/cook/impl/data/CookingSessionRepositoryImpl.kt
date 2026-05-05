package com.plusmobileapps.chefmate.cook.impl.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.plusmobileapps.chefmate.cook.data.CookingSessionRepository
import com.plusmobileapps.chefmate.database.CookingSessionQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class CookingSessionRepositoryImpl(
    private val queries: CookingSessionQueries,
    @IO private val ioContext: CoroutineContext,
) : CookingSessionRepository {

    override fun observeRecipeIds(): Flow<List<Long>> =
        queries.readRecipeIds().asFlow().mapToList(ioContext)

    override suspend fun start(recipeId: Long) {
        withContext(ioContext) { queries.start(recipeId) }
    }

    override suspend fun markSelected(recipeId: Long) {
        withContext(ioContext) { queries.markSelected(recipeId) }
    }

    override suspend fun stop(recipeIds: List<Long>) {
        if (recipeIds.isEmpty()) return
        withContext(ioContext) { queries.deleteByRecipeIds(recipeIds) }
    }

    override suspend fun stopAll() {
        withContext(ioContext) { queries.deleteAll() }
    }
}
