package com.plusmobileapps.chefmate.recipe.list

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import kotlinx.coroutines.flow.StateFlow

interface RecipeListBloc {
    val state: StateFlow<Model>

    fun onRecipeClicked(recipe: RecipeListItem)

    fun onAddRecipeClicked()

    fun onDeleteRecipe(recipe: RecipeListItem)

    fun onToggleFavorite(recipe: RecipeListItem)

    fun onSortOptionSelected(option: RecipeSortOption)

    fun onFilterToggled(filter: RecipeFilterOption)

    fun onToggleViewMode()

    fun onSearchQueryChanged(query: String)

    data class Model(
        val recipes: List<RecipeListItem> = emptyList(),
        val isLoading: Boolean = false,
        val currentSort: RecipeSortOption = RecipeSortOption.RECENTLY_ADDED,
        val activeFilters: Set<RecipeFilterOption> = emptySet(),
        val isGridView: Boolean = false,
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,
    )

    sealed class Output {
        data class OpenRecipe(val recipeId: Long) : Output()

        object AddNewRecipe : Output()
    }

    interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): RecipeListBloc
    }
}
