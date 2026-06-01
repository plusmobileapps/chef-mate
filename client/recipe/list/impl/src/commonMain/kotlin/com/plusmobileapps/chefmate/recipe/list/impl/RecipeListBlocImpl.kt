package com.plusmobileapps.chefmate.recipe.list.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.list.RecipeFilterOption
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc.Output
import com.plusmobileapps.chefmate.recipe.list.RecipeListItem
import com.plusmobileapps.chefmate.recipe.list.RecipeSortOption
import com.plusmobileapps.chefmate.recipe.list.impl.ui.RecipeListScreen
import com.plusmobileapps.chefmate.util.TimeFormatterUtil
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = RecipeListBloc.Factory::class,
)
class RecipeListBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: Provider<RecipeListViewModel>,
    private val timeFormatterUtil: TimeFormatterUtil,
) : RecipeListBloc, BlocContext by context {
    private val viewModel: RecipeListViewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<RecipeListBloc.Model> =
        viewModel.state.mapState {
            RecipeListBloc.Model(
                isLoading = it.isLoading,
                isSyncing = it.isSyncing,
                recipes = it.displayRecipes.map { recipe -> recipe.toRecipeListItem() },
                totalRecipeCount = it.recipes.size,
                currentSort = it.currentSort,
                activeFilters = it.activeFilters,
                activeCategories = it.activeCategories,
                activeUserCategoryIds = it.activeUserCategoryIds,
                availableUserCategories = it.availableUserCategories,
                isGridView = it.isGridView,
                searchQuery = it.searchQuery,
                isSearchActive = it.isSearchActive,
                cookingRecipeCount = it.cookingRecipeIds.size,
                showDoneCookingDialog = it.showDoneCookingDialog,
                isSelectionMode = it.isSelectionMode,
                selectedRecipeIds = it.selectedRecipeIds,
            )
        }

    override fun onRecipeClicked(recipe: RecipeListItem) {
        if (viewModel.state.value.isSelectionMode) {
            viewModel.toggleRecipeSelected(recipe.id)
        } else {
            output.onNext(Output.OpenRecipe(recipe.id))
        }
    }

    override fun onAddRecipeClicked() {
        output.onNext(Output.AddNewRecipe)
    }

    override fun onDeleteRecipe(recipe: RecipeListItem) {
        viewModel.deleteRecipe(recipe.id)
    }

    override fun onToggleFavorite(recipe: RecipeListItem) {
        viewModel.toggleFavorite(recipe.id)
    }

    override fun onSortOptionSelected(option: RecipeSortOption) {
        viewModel.updateSort(option)
    }

    override fun onFilterToggled(filter: RecipeFilterOption) {
        viewModel.toggleFilter(filter)
    }

    override fun onToggleViewMode() {
        viewModel.toggleViewMode()
    }

    override fun onSearchQueryChanged(query: String) {
        viewModel.updateSearchQuery(query)
    }

    override fun onClearFilters() {
        viewModel.clearFilters()
    }

    override fun onApplySortAndFilters(
        sort: RecipeSortOption,
        filters: Set<RecipeFilterOption>,
        categories: Set<BuiltinCategory>,
        userCategoryIds: Set<Long>,
    ) {
        viewModel.applySortAndFilters(sort, filters, categories, userCategoryIds)
    }

    override fun onBrowseRecipesClicked() {
        output.onNext(Output.OpenBrowser)
    }

    override fun onSyncClicked() {
        viewModel.onSyncClicked()
    }

    override fun onContinueCookingClicked() {
        viewModel.state.value.cookingRecipeIds.firstOrNull()?.let { recipeId ->
            output.onNext(Output.OpenCookMode(recipeId))
        }
    }

    override fun onDoneCookingClicked() {
        viewModel.showDoneCookingDialog()
    }

    override fun onDoneCookingConfirmed() {
        viewModel.confirmDoneCooking()
    }

    override fun onDoneCookingDismissed() {
        viewModel.dismissDoneCookingDialog()
    }

    override fun onEnterSelectionMode() {
        viewModel.enterSelectionMode()
    }

    override fun onExitSelectionMode() {
        viewModel.exitSelectionMode()
    }

    override fun onToggleRecipeSelected(recipe: RecipeListItem) {
        viewModel.toggleRecipeSelected(recipe.id)
    }

    override fun onToggleSelectAllVisible() {
        viewModel.toggleSelectAllVisible()
    }

    override fun onExportClicked() {
        val state = viewModel.state.value
        val ids = if (state.isSelectionMode) state.selectedRecipeIds else null
        output.onNext(Output.OpenExportRecipes(ids))
        if (state.isSelectionMode) viewModel.exitSelectionMode()
    }

    @Composable
    override fun Content(modifier: Modifier) {
        RecipeListScreen(bloc = this, modifier = modifier)
    }

    private fun Recipe.toRecipeListItem(): RecipeListItem =
        RecipeListItem(
            id = id,
            title = title,
            description = description,
            imageUrl = imageUrl,
            starRating = starRating,
            totalTime = totalTime,
            formattedTotalTime = totalTime?.let { timeFormatterUtil.formatMinutes(it) },
            servings = servings,
            calories = calories,
            isFavorite = isFavorite,
            syncStatus = syncStatus,
        )
}
