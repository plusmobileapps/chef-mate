package com.plusmobileapps.chefmate.recipebook.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RecipeBookRepository {
    fun getRecipeBooks(): Flow<List<RecipeBook>>

    fun getRecipeBook(id: Long): Flow<RecipeBook?>

    /**
     * The local id of the currently active book — the one whose recipes the list shows and that new
     * recipes are filed under. Backed by persisted settings; resolves to the default book when the
     * user hasn't picked one yet.
     *
     * Null carries two meanings that callers disambiguate by lifecycle: it is the transient value
     * before the default book has been created, and — once [selectAllRecipes] has been chosen — the
     * persisted "All recipes" selection that spans every book. New recipes still file under the
     * default book while "All recipes" is active.
     */
    val activeBookId: StateFlow<Long?>

    /** Ensures the default book exists and returns its local id. */
    suspend fun getDefaultBookId(): Long

    suspend fun setActiveBook(id: Long)

    /**
     * Selects the cross-book "All recipes" view: clears the active book so the list shows recipes
     * from every book and search spans all of them. Persisted like [setActiveBook].
     */
    suspend fun selectAllRecipes()

    suspend fun createBook(name: String): RecipeBook

    suspend fun renameBook(id: Long, name: String): RecipeBook

    suspend fun syncAllUnsynced()

    suspend fun clearLocalData()
}
