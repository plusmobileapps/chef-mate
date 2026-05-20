package com.plusmobileapps.chefmate.grocery.core.impl.list

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc.GroceryFilter
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc.GrocerySort
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = GroceryListBloc.Factory::class,
)
class GroceryListBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<GroceryListBloc.Output>,
    viewModelFactory: Provider<GroceryListViewModel>,
) : GroceryListBloc, BlocContext by context {

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<GroceryListBloc.Model> =
        viewModel.state.mapState {
            GroceryListBloc.Model(
                groupedItems = it.groupedItems,
                sort = it.sort,
                filter = it.filter,
                recipeFilter = it.recipeFilter,
                availableRecipes = it.availableRecipes,
                hasNoRecipeItems = it.hasNoRecipeItems,
                isSyncing = it.isSyncing,
                lists = it.lists,
                selectedList = it.selectedList,
                showCreateListDialog = it.showCreateListDialog,
                showDeleteDialog = it.showDeleteDialog,
                showListSelector = it.showListSelector,
            )
        }

    override val newGroceryItemName: StateFlow<String> = viewModel.newGroceryItemName

    override fun onGroceryItemCheckedChange(item: GroceryItem, isChecked: Boolean) {
        viewModel.onGroceryItemCheckedChange(item, isChecked)
    }

    override fun onGroceryItemDelete(item: GroceryItem) {
        viewModel.onGroceryItemDelete(item)
    }

    override fun onNewGroceryItemNameChange(name: String) {
        if (name.contains("\n")) {
            viewModel.saveGroceryItem()
        } else {
            viewModel.onNewGroceryItemNameChange(name)
        }
    }

    override fun saveGroceryItem() {
        viewModel.saveGroceryItem()
    }

    override fun onGroceryItemClicked(item: GroceryItem) {
        output.onNext(GroceryListBloc.Output.OpenDetail(item.id))
    }

    override fun onSyncClicked() {
        viewModel.onSyncClicked()
    }

    override fun onListSelected(list: GroceryListModel) {
        viewModel.onListSelected(list)
    }

    override fun onCreateListClicked() {
        viewModel.onCreateListClicked()
    }

    override fun onCreateListDismissed() {
        viewModel.onCreateListDismissed()
    }

    override fun onCreateListConfirmed(name: String) {
        viewModel.onCreateListConfirmed(name)
    }

    override fun onDeleteListClicked(list: GroceryListModel) {
        viewModel.onDeleteListClicked(list)
    }

    override fun onApplySortAndFilter(
        sort: GrocerySort,
        filter: GroceryFilter,
        recipeFilter: String?,
    ) {
        viewModel.onApplySortAndFilter(sort, filter, recipeFilter)
    }

    override fun onDeleteClicked() {
        viewModel.onDeleteClicked()
    }

    override fun onDeleteDismissed() {
        viewModel.onDeleteDismissed()
    }

    override fun onDeletePurchasedConfirmed() {
        viewModel.onDeletePurchasedConfirmed()
    }

    override fun onDeleteAllConfirmed() {
        viewModel.onDeleteAllConfirmed()
    }

    override fun onListSelectorClicked() {
        viewModel.onListSelectorClicked()
    }

    override fun onListSelectorDismissed() {
        viewModel.onListSelectorDismissed()
    }

    override fun onBrowseRecipesClicked() {
        output.onNext(GroceryListBloc.Output.OpenRecipes)
    }
}
