package com.plusmobileapps.chefmate.grocerylist

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import kotlinx.coroutines.flow.StateFlow

class GroceryListBlocImpl(
    context: BlocContext,
) : GroceryListBloc, BlocContext by context {

    private val viewModel = instanceKeeper.getViewModel {
        GroceryListViewModel(mainContext)
    }

    override val state: StateFlow<GroceryListBloc.Model> = viewModel.state.mapState {
        GroceryListBloc.Model(
            items = it.items
        )
    }

    override val newGroceryItemName: StateFlow<String> = viewModel.newGroceryItemName

    override fun onGroceryItemCheckedChange(
        item: GroceryItem,
        isChecked: Boolean
    ) {
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
}