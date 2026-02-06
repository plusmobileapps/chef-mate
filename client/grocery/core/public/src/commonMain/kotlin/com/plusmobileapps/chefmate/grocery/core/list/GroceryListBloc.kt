package com.plusmobileapps.chefmate.grocery.core.list

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import kotlinx.coroutines.flow.StateFlow

interface GroceryListBloc {
    val state: StateFlow<Model>

    val newGroceryItemName: StateFlow<String>

    fun onGroceryItemCheckedChange(
        item: GroceryItem,
        isChecked: Boolean,
    )

    fun onGroceryItemDelete(item: GroceryItem)

    fun onNewGroceryItemNameChange(name: String)

    fun saveGroceryItem()

    fun onGroceryItemClicked(item: GroceryItem)

    fun onSyncClicked()

    data class Model(
        val items: List<GroceryItem> = emptyList(),
    )

    sealed class Output {
        data class OpenDetail(
            val id: Long,
        ) : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            output: Consumer<Output>,
        ): GroceryListBloc
    }
}
