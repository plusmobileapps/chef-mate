package com.plusmobileapps.chefmate.cook.data

import kotlinx.coroutines.flow.Flow

interface CookingSessionRepository {
    /** Recipe ids in last-selected-first order. */
    fun observeRecipeIds(): Flow<List<Long>>

    suspend fun start(recipeId: Long)

    /**
     * Replace the current cooking session with [recipeIds]: stops all in-flight recipes, starts
     * each of the new ones, and marks the first as selected so cook mode opens to it. No-op when
     * [recipeIds] is empty.
     */
    suspend fun replaceAll(recipeIds: List<Long>)

    /** Bumps the recipe's last-selected timestamp so it sorts to the top. */
    suspend fun markSelected(recipeId: Long)

    suspend fun stop(recipeIds: List<Long>)

    suspend fun stopAll()
}
