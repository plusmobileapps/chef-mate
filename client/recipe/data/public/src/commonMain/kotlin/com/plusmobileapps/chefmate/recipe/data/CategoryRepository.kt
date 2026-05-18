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
interface CategoryRepository {
    /**
     * Emits the user's persisted categories. Includes both materialized presets and user-created.
     */
    fun observeUserCategories(): Flow<List<Category>>

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
