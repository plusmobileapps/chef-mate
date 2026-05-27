package com.plusmobileapps.chefmate.recipe.list

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.coroutines.flow.StateFlow

interface RecipeListBloc : BlocScreen {
    val state: StateFlow<Model>

    fun onRecipeClicked(recipe: RecipeListItem)

    fun onAddRecipeClicked()

    fun onDeleteRecipe(recipe: RecipeListItem)

    fun onToggleFavorite(recipe: RecipeListItem)

    fun onSortOptionSelected(option: RecipeSortOption)

    fun onFilterToggled(filter: RecipeFilterOption)

    fun onToggleViewMode()

    fun onSearchQueryChanged(query: String)

    fun onClearFilters()

    fun onApplySortAndFilters(
        sort: RecipeSortOption,
        filters: Set<RecipeFilterOption>,
        categories: Set<BuiltinCategory>,
        userCategoryIds: Set<Long>,
    )

    fun onBrowseRecipesClicked()

    fun onSyncClicked()

    fun onContinueCookingClicked()

    fun onDoneCookingClicked()

    fun onDoneCookingConfirmed()

    fun onDoneCookingDismissed()

    data class Model(
        val recipes: List<RecipeListItem> = emptyList(),
        val totalRecipeCount: Int = 0,
        val isLoading: Boolean = false,
        val isSyncing: Boolean = false,
        val currentSort: RecipeSortOption = RecipeSortOption.RECENTLY_ADDED,
        val activeFilters: Set<RecipeFilterOption> = emptySet(),
        val activeCategories: Set<BuiltinCategory> = emptySet(),
        /** Selected user-category IDs from the filter sheet. Disjoint from [activeCategories]. */
        val activeUserCategoryIds: Set<Long> = emptySet(),
        /** All user-created categories — surfaced so the filter sheet can render them as chips. */
        val availableUserCategories: List<Category> = emptyList(),
        val isGridView: Boolean = false,
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,
        val cookingRecipeCount: Int = 0,
        val showDoneCookingDialog: Boolean = false,
    ) {
        /** Total number of active filter chips: legacy filters + preset + user category filters. */
        val totalActiveFilterCount: Int
            get() = activeFilters.size + activeCategories.size + activeUserCategoryIds.size
    }

    sealed class Output {
        data class OpenRecipe(val recipeId: Long) : Output()

        object AddNewRecipe : Output()

        object OpenBrowser : Output()

        data class OpenCookMode(val recipeId: Long) : Output()
    }

    interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): RecipeListBloc
    }
}
