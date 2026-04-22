package com.plusmobileapps.chefmate.grocery.core.snapshots

import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc.GroceryFilter
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class StubGroceryListBloc(
    model: GroceryListBloc.Model = GroceryListBloc.Model(),
    newItemName: String = "",
) : GroceryListBloc {
    override val state: StateFlow<GroceryListBloc.Model> = MutableStateFlow(model)
    override val newGroceryItemName: StateFlow<String> = MutableStateFlow(newItemName)

    override fun onGroceryItemCheckedChange(item: GroceryItem, isChecked: Boolean) {}

    override fun onGroceryItemDelete(item: GroceryItem) {}

    override fun onNewGroceryItemNameChange(name: String) {}

    override fun saveGroceryItem() {}

    override fun onGroceryItemClicked(item: GroceryItem) {}

    override fun onSyncClicked() {}

    override fun onListSelected(list: GroceryListModel) {}

    override fun onCreateListClicked() {}

    override fun onCreateListDismissed() {}

    override fun onCreateListConfirmed(name: String) {}

    override fun onDeleteListClicked(list: GroceryListModel) {}

    override fun onFilterChanged(filter: GroceryFilter) {}

    override fun onDeleteClicked() {}

    override fun onDeleteDismissed() {}

    override fun onDeletePurchasedConfirmed() {}

    override fun onDeleteAllConfirmed() {}

    override fun onListSelectorClicked() {}

    override fun onListSelectorDismissed() {}
}
