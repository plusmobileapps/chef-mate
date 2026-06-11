package com.plusmobileapps.chefmate.recipe.list

import com.plusmobileapps.chefmate.recipe.data.SyncStatus
import com.plusmobileapps.chefmate.text.TextData

data class RecipeListItem(
    val id: Long,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val starRating: Int?,
    val totalTime: Int?,
    val formattedTotalTime: TextData?,
    val servings: Int?,
    val calories: Int?,
    val isFavorite: Boolean,
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
    /**
     * Name of the recipe book(s) this recipe lives in, shown only for cross-book search results.
     * Null when the result set is scoped to a single book, which hides the label.
     */
    val bookName: String? = null,
)
