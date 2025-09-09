package com.plusmobileapps.chefmate.grocerylist.list

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocerylist.GroceryItem
import kotlinx.coroutines.flow.StateFlow

interface GroceryListBloc {

    val state: StateFlow<Model>

    val newGroceryItemName: StateFlow<String>

    fun onGroceryItemCheckedChange(item: GroceryItem, isChecked: Boolean)

    fun onGroceryItemDelete(item: GroceryItem)

    fun onNewGroceryItemNameChange(name: String)

    fun saveGroceryItem()

    fun onGroceryItemClicked(item: GroceryItem)

    data class Model(
        val items: List<GroceryItem> = emptyList(),
    )

    sealed class Output {
        data class OpenDetail(val id: Long) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): GroceryListBloc
    }
}