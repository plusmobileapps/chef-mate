package com.plusmobileapps.chefmate.recipe.data

import kotlinx.coroutines.flow.Flow

/**
 * Manages [Category] rows for the current user. Built-in presets ([BuiltinCategory]) live as code
 * constants; they only become a [Category] DB row once [materializeBuiltin] is called (typically
 * the first time a user attaches a preset to a recipe).
 *
 * The picker UI is responsible for merging [observeUserCategories] with [BuiltinCategory.entries] —
 * this repo only deals in concrete rows.
 */
/**
 * A [Category] paired with how many recipes currently reference it. Synthetic preset rows that have
 * not yet been materialized are reported with [recipeCount] of 0 and a non-persistent id.
 */
data class CategoryWithCount(val category: Category, val recipeCount: Int)

interface CategoryRepository {
    /**
     * Emits the user's persisted categories. Includes both materialized presets and user-created.
     */
    fun observeUserCategories(): Flow<List<Category>>

    /**
     * Emits every category surfaced to the user — concrete rows from [observeUserCategories] plus
     * synthetic rows for any [BuiltinCategory] that has not been materialized yet — each paired
     * with its current recipe count.
     */
    fun observeCategoriesWithCounts(): Flow<List<CategoryWithCount>>

    /** Looks up an already-materialized preset row, or null if not yet materialized. */
    suspend fun findBuiltin(builtin: BuiltinCategory): Category?

    /**
     * Returns the [Category] row backing [builtin], creating it if it doesn't exist yet. Safe to
     * call repeatedly — the partial unique index on `(ownerId, builtinId)` keeps it idempotent.
     */
    suspend fun materializeBuiltin(builtin: BuiltinCategory): Category

    /**
     * Creates a new user-defined category. The repo enforces no name uniqueness — that's a UI
     * concern.
     */
    suspend fun createUserCategory(name: String): Category

    suspend fun renameCategory(id: Long, name: String): Category

    suspend fun deleteCategory(id: Long)

    suspend fun clearLocalData()
}
