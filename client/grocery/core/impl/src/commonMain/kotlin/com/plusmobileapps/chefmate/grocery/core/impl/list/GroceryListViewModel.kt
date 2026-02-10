@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.grocery.core.impl.list

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import kotlin.coroutines.CoroutineContext

@Inject
class GroceryListViewModel(
    @Main mainContext: CoroutineContext,
    private val repository: GroceryRepository,
) : ViewModel(mainContext) {
    private val _state = MutableStateFlow(State())
    private val _newGroceryItemName = MutableStateFlow("")
    private val selectedListId = MutableStateFlow<Long?>(null)

    val state: StateFlow<State> = _state.asStateFlow()

    val newGroceryItemName: StateFlow<String> = _newGroceryItemName.asStateFlow()

    init {
        scope.launch {
            val defaultListId = repository.ensureDefaultList()
            if (selectedListId.value == null) {
                selectedListId.value = defaultListId
            }
        }

        scope.launch {
            repository.getGroceryLists().collect { lists ->
                _state.update { currentState ->
                    val selected = currentState.selectedList
                    val updatedSelected = if (selected != null) {
                        lists.firstOrNull { it.id == selected.id } ?: lists.firstOrNull()
                    } else {
                        lists.firstOrNull()
                    }
                    if (updatedSelected != null && selectedListId.value != updatedSelected.id) {
                        selectedListId.value = updatedSelected.id
                    }
                    currentState.copy(
                        lists = lists,
                        selectedList = updatedSelected,
                    )
                }
            }
        }

        scope.launch {
            selectedListId
                .flatMapLatest { listId ->
                    if (listId != null) {
                        repository.getGroceries(listId)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collect { items ->
                    _state.update { it.copy(items = items) }
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
        val listId = selectedListId.value ?: return
        scope.launch {
            repository.addGrocery(listId, name)
        }
        _newGroceryItemName.value = ""
    }

    fun onSyncClicked() {
        scope.launch {
            _state.update { it.copy(isSyncing = true) }
            try {
                repository.syncAllUnsynced()
            } finally {
                _state.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun onListSelected(list: GroceryListModel) {
        selectedListId.value = list.id
        _state.update { it.copy(selectedList = list) }
    }

    fun onCreateListClicked() {
        _state.update { it.copy(showCreateListDialog = true) }
    }

    fun onCreateListDismissed() {
        _state.update { it.copy(showCreateListDialog = false) }
    }

    fun onCreateListConfirmed(name: String) {
        if (name.isBlank()) return
        _state.update { it.copy(showCreateListDialog = false) }
        scope.launch {
            val newId = repository.createGroceryList(name)
            selectedListId.value = newId
        }
    }

    fun onDeleteListClicked(list: GroceryListModel) {
        scope.launch {
            repository.deleteGroceryList(list.id)
            if (selectedListId.value == list.id) {
                val remaining = _state.value.lists.filter { it.id != list.id }
                val next = remaining.firstOrNull()
                selectedListId.value = next?.id
                _state.update { it.copy(selectedList = next) }
            }
        }
    }

    data class State(
        val items: List<GroceryItem> = emptyList(),
        val isSyncing: Boolean = false,
        val lists: List<GroceryListModel> = emptyList(),
        val selectedList: GroceryListModel? = null,
        val showCreateListDialog: Boolean = false,
    )
}
