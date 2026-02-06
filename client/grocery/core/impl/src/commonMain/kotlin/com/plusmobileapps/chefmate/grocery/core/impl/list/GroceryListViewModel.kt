@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.grocery.core.impl.list

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime

@Inject
class GroceryListViewModel(
    @Main mainContext: CoroutineContext,
    private val repository: GroceryRepository,
) : ViewModel(mainContext) {
    private val _state = MutableStateFlow(State())
    private val _newGroceryItemName = MutableStateFlow("")

    val state: StateFlow<State> = _state.asStateFlow()

    val newGroceryItemName: StateFlow<String> = _newGroceryItemName.asStateFlow()

    init {
        scope.launch {
            repository.getGroceries().collect {
                _state.update { currentState ->
                    currentState.copy(items = it)
                }
            }
        }
    }

    fun onGroceryItemCheckedChange(
        item: GroceryItem,
        isChecked: Boolean,
    ) {
        scope.launch {
            repository.updateChecked(item, isChecked)
        }
    }

    fun onGroceryItemDelete(item: GroceryItem) {
        scope.launch {
            repository.deleteGrocery(item)
        }
    }

    fun onNewGroceryItemNameChange(name: String) {
        _newGroceryItemName.value = name
    }

    fun saveGroceryItem() {
        val name = newGroceryItemName.value
        if (name.isBlank()) return
        scope.launch {
            repository.addGrocery(name)
        }
        _newGroceryItemName.value = ""
    }

    fun onSyncClicked() {
        scope.launch {
            repository.syncAllUnsynced()
        }
    }

    data class State(
        val items: List<GroceryItem> = emptyList(),
    )
}
