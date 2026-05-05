package com.plusmobileapps.chefmate.cook.data

import kotlinx.coroutines.flow.Flow

interface CookingSessionRepository {
    /** Recipe ids in last-selected-first order. */
    fun observeRecipeIds(): Flow<List<Long>>

    suspend fun start(recipeId: Long)

    /** Bumps the recipe's last-selected timestamp so it sorts to the top. */
    suspend fun markSelected(recipeId: Long)

    suspend fun stop(recipeIds: List<Long>)

    suspend fun stopAll()
}
