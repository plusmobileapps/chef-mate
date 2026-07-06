package com.plusmobileapps.chefmate.grocery.data

import kotlinx.coroutines.flow.Flow

/** A single user-defined grocery autocomplete entry. */
data class GroceryAutocompleteItem(val id: Long, val name: String)

/**
 * Manages the user's custom grocery autocomplete entries. These are surfaced alongside the built-in
 * [IngredientParser] vocabulary when suggesting grocery item names, and are managed from the
 * grocery autocomplete settings screen.
 */
interface GroceryAutocompleteRepository {
    /** Emits the user's saved autocomplete items, sorted case-insensitively by name. */
    fun observeItems(): Flow<List<GroceryAutocompleteItem>>

    /**
     * Adds [name] as a saved item. Trimmed; duplicates (case-insensitive) and blanks are ignored.
     */
    suspend fun addItem(name: String)

    suspend fun deleteItem(id: Long)

    suspend fun clearLocalData()
}
