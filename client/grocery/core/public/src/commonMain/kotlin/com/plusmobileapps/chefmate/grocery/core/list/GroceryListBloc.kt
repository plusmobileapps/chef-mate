package com.plusmobileapps.chefmate.grocery.core.list

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import kotlinx.coroutines.flow.StateFlow

interface GroceryListBloc {
    val state: StateFlow<Model>

    val newGroceryItemName: StateFlow<String>

    fun onGroceryItemCheckedChange(item: GroceryItem, isChecked: Boolean)

    fun onGroceryItemDelete(item: GroceryItem)

    fun onNewGroceryItemNameChange(name: String)

    fun saveGroceryItem()

    fun onGroceryItemClicked(item: GroceryItem)

    fun onSyncClicked()

    fun onListSelected(list: GroceryListModel)

    fun onCreateListClicked()

    fun onCreateListDismissed()

    fun onCreateListConfirmed(name: String)

    fun onDeleteListClicked(list: GroceryListModel)

    fun onApplySortAndFilter(sort: GrocerySort, filter: GroceryFilter)

    fun onDeleteClicked()

    fun onDeleteDismissed()

    fun onDeletePurchasedConfirmed()

    fun onDeleteAllConfirmed()

    fun onListSelectorClicked()

    fun onListSelectorDismissed()

    data class GroceryGroup(val category: GroceryCategory, val items: List<GroceryItem>)

    enum class GrocerySort {
        AISLE,
        ALPHABETICAL,
    }

    enum class GroceryFilter {
        ALL,
        UNPURCHASED,
        PURCHASED,
    }

    data class Model(
        val groupedItems: List<GroceryGroup> = emptyList(),
        val sort: GrocerySort = GrocerySort.AISLE,
        val filter: GroceryFilter = GroceryFilter.ALL,
        val isSyncing: Boolean = false,
        val lists: List<GroceryListModel> = emptyList(),
        val selectedList: GroceryListModel? = null,
        val showCreateListDialog: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val showListSelector: Boolean = false,
    )

    sealed class Output {
        data class OpenDetail(val id: Long) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): GroceryListBloc
    }
}
