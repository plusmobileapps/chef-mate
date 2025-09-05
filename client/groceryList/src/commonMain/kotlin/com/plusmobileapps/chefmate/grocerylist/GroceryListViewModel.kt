@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.grocerylist

import com.plusmobileapps.chefmate.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class GroceryListViewModel(
    mainContext: CoroutineContext,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(State())
    private val _newGroceryItemName = MutableStateFlow("")

    val state: StateFlow<State> = _state.asStateFlow()

    val newGroceryItemName: StateFlow<String> = _newGroceryItemName.asStateFlow()

    fun onGroceryItemCheckedChange(
        item: GroceryItem,
        isChecked: Boolean
    ) {
        _state.update { currentState ->
            val newItems = currentState.items.map {
                if (it.id == item.id) {
                    it.copy(isChecked = isChecked)
                } else {
                    it
                }
            }
            currentState.copy(items = newItems)
        }
    }

    fun onGroceryItemDelete(item: GroceryItem) {
        _state.update { currentState ->
            val newItems = currentState.items.filter {
                it.id != item.id
            }
            currentState.copy(items = newItems)
        }
    }

    fun onNewGroceryItemNameChange(name: String) {
        _newGroceryItemName.value = name
    }

    fun saveGroceryItem() {
        val name = newGroceryItemName.value
        if (name.isBlank()) return

        val newItem = GroceryItem(
            id = Clock.System.now().epochSeconds,
            name = name,
            isChecked = false
        )
        _state.update { currentState ->
            currentState.copy(items = currentState.items + newItem)
        }
        _newGroceryItemName.value = ""
    }

    data class State(
        val items: List<GroceryItem> = emptyList(),
    )

}