package com.plusmobileapps.chefmate.recipebook.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RecipeBookRepository {
    fun getRecipeBooks(): Flow<List<RecipeBook>>

    fun getRecipeBook(id: Long): Flow<RecipeBook?>

    /**
     * The local id of the currently active book — the one whose recipes the list shows and that new
     * recipes are filed under. Backed by persisted settings; resolves to the default book when the
     * user hasn't picked one yet. Null only before the default book has been created.
     */
    val activeBookId: StateFlow<Long?>

    /** Ensures the default book exists and returns its local id. */
    suspend fun getDefaultBookId(): Long

    suspend fun setActiveBook(id: Long)

    suspend fun createBook(name: String): RecipeBook

    suspend fun renameBook(id: Long, name: String): RecipeBook

    suspend fun syncAllUnsynced()

    suspend fun clearLocalData()
}
