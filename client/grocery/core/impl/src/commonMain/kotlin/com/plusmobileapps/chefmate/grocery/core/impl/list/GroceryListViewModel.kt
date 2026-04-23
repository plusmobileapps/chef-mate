@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.grocery.core.impl.list

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc.GroceryFilter
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc.GroceryGroup
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class GroceryListViewModel(
    @Main mainContext: CoroutineContext,
    private val repository: GroceryRepository,
    private val authRepository: AuthenticationRepository,
) : ViewModel(mainContext) {
    private val _state = MutableStateFlow(State())
    private val _newGroceryItemName = MutableStateFlow("")
    private val selectedListId = MutableStateFlow<Long?>(null)
    private val _filter = MutableStateFlow(GroceryFilter.ALL)
    // Set to true when the user signs in during this VM's lifetime; cleared once lists arrive.
    private val _showListSelectorAfterSignIn = MutableStateFlow(false)

    val state: StateFlow<State> = _state.asStateFlow()

    val newGroceryItemName: StateFlow<String> = _newGroceryItemName.asStateFlow()

    init {
        scope.launch {
            // When not yet authenticated on start, ensure a default list exists immediately.
            // Authenticated users get their lists from the remote sync triggered by the repository.
            if (authRepository.state.value !is AuthState.Authenticated) {
                val defaultListId = repository.ensureDefaultList()
                if (selectedListId.value == null) {
                    selectedListId.value = defaultListId
                }
            }

            // Watch for subsequent sign-in transitions (skip the initial emission).
            authRepository.state.drop(1).collect { state ->
                if (state is AuthState.Authenticated) {
                    _showListSelectorAfterSignIn.value = true
                }
            }
        }

        scope.launch {
            repository.getGroceryLists().collect { lists ->
                _state.update { currentState ->
                    val currentSelectedId = selectedListId.value
                    val updatedSelected =
                        lists.firstOrNull { it.id == currentSelectedId } ?: lists.firstOrNull()
                    if (updatedSelected != null && selectedListId.value != updatedSelected.id) {
                        selectedListId.value = updatedSelected.id
                    }
                    // After sign-in, show the list selector once lists arrive from sync so the
                    // user can pick which list to use rather than a new default being
                    // auto-selected.
                    val shouldShowSelector =
                        _showListSelectorAfterSignIn.value && lists.isNotEmpty()
                    if (shouldShowSelector) _showListSelectorAfterSignIn.value = false
                    currentState.copy(
                        lists = lists,
                        selectedList = updatedSelected,
                        showListSelector = currentState.showListSelector || shouldShowSelector,
                    )
                }
            }
        }

        scope.launch {
            combine(
                    selectedListId.flatMapLatest { listId ->
                        if (listId != null) {
                            repository.getGroceries(listId)
                        } else {
                            flowOf(emptyList())
                        }
                    },
                    _filter,
                ) { items, filter ->
                    val filtered =
                        when (filter) {
                            GroceryFilter.ALL -> items
                            GroceryFilter.UNPURCHASED -> items.filter { !it.isChecked }
                            GroceryFilter.PURCHASED -> items.filter { it.isChecked }
                        }
                    val grouped =
                        filtered
                            .groupBy { it.category }
                            .entries
                            .sortedBy { it.key.ordinal }
                            .map { (category, categoryItems) ->
                                GroceryGroup(category = category, items = categoryItems)
                            }
                    grouped
                }
                .collect { grouped -> _state.update { it.copy(groupedItems = grouped) } }
        }
    }

    fun onGroceryItemCheckedChange(item: GroceryItem, isChecked: Boolean) {
        scope.launch { repository.updateChecked(item, isChecked) }
    }

    fun onGroceryItemDelete(item: GroceryItem) {
        scope.launch { repository.deleteGrocery(item) }
    }

    fun onNewGroceryItemNameChange(name: String) {
        _newGroceryItemName.value = name
    }

    fun saveGroceryItem() {
        val name = newGroceryItemName.value
        if (name.isBlank()) return
        val listId = selectedListId.value ?: return
        scope.launch { repository.addGrocery(listId, name) }
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
        _state.update { it.copy(selectedList = list, showListSelector = false) }
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

    fun onFilterChanged(filter: GroceryFilter) {
        _filter.value = filter
        _state.update { it.copy(filter = filter) }
    }

    fun onDeleteClicked() {
        _state.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteDismissed() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    fun onDeletePurchasedConfirmed() {
        val listId = selectedListId.value ?: return
        _state.update { it.copy(showDeleteDialog = false) }
        scope.launch { repository.deletePurchasedGroceries(listId) }
    }

    fun onListSelectorClicked() {
        _state.update { it.copy(showListSelector = true) }
    }

    fun onListSelectorDismissed() {
        _state.update { it.copy(showListSelector = false) }
    }

    fun onDeleteAllConfirmed() {
        val listId = selectedListId.value ?: return
        _state.update { it.copy(showDeleteDialog = false) }
        scope.launch { repository.deleteAllGroceries(listId) }
    }

    data class State(
        val groupedItems: List<GroceryGroup> = emptyList(),
        val filter: GroceryFilter = GroceryFilter.ALL,
        val isSyncing: Boolean = false,
        val lists: List<GroceryListModel> = emptyList(),
        val selectedList: GroceryListModel? = null,
        val showCreateListDialog: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val showListSelector: Boolean = false,
    )
}
