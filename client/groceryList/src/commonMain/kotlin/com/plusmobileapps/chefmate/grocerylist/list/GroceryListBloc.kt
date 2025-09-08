package com.plusmobileapps.chefmate.grocerylist.list

import com.plusmobileapps.chefmate.grocerylist.GroceryItem
import kotlinx.coroutines.flow.StateFlow

interface GroceryListBloc {

    val state: StateFlow<Model>

    val newGroceryItemName: StateFlow<String>

    fun onGroceryItemCheckedChange(item: GroceryItem, isChecked: Boolean)

    fun onGroceryItemDelete(item: GroceryItem)

    fun onNewGroceryItemNameChange(name: String)

    fun saveGroceryItem()

    data class Model(
        val items: List<GroceryItem> = emptyList(),
    )
}