package com.plusmobileapps.chefmate.recipe.list

/**
 * Which recipe books a search session reads from. A scope is a *temporary* view that never changes
 * the persisted active book — it is reset when the search is cleared or the active book is
 * switched.
 */
sealed interface RecipeSearchScope {
    /** Search across every recipe book the user can see. */
    data object AllBooks : RecipeSearchScope

    /** Search within a single recipe book (by local id). */
    data class Book(val bookId: Long) : RecipeSearchScope
}
