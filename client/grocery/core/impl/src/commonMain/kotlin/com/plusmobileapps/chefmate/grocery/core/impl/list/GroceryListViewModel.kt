@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.grocery.core.impl.list

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc.GroceryFilter
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc.GroceryGroup
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc.GrocerySort
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.russhwolf.settings.Settings
import com.russhwolf.settings.string
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class GroceryListViewModel(
    @Main mainContext: CoroutineContext,
    private val repository: GroceryRepository,
    settings: Settings,
) : ViewModel(mainContext) {
    private var sortPref by settings.string(KEY_SORT, GrocerySort.AISLE.name)
    private var filterPref by settings.string(KEY_FILTER, GroceryFilter.ALL.name)

    private val initialSort = GrocerySort.entries.find { it.name == sortPref } ?: GrocerySort.AISLE
    private val initialFilter =
        GroceryFilter.entries.find { it.name == filterPref } ?: GroceryFilter.ALL

    private val _state = MutableStateFlow(State(sort = initialSort, filter = initialFilter))
    private val _newGroceryItemName = MutableStateFlow("")
    private val selectedListId = MutableStateFlow<Long?>(null)
    private val _sort = MutableStateFlow(initialSort)
    private val _filter = MutableStateFlow(initialFilter)
    private val _recipeFilter = MutableStateFlow<String?>(null)

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
                    val currentSelectedId = selectedListId.value
                    val updatedSelected =
                        lists.firstOrNull { it.id == currentSelectedId } ?: lists.firstOrNull()
                    if (updatedSelected != null && selectedListId.value != updatedSelected.id) {
                        selectedListId.value = updatedSelected.id
                    }
                    currentState.copy(lists = lists, selectedList = updatedSelected)
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
                    _sort,
                    _recipeFilter,
                ) { items, filter, sort, recipeFilter ->
                    val availableRecipes = items.mapNotNull { it.recipeName }.distinct().sorted()
                    val hasNoRecipeItems = items.any { it.recipeName == null }
                    val filtered =
                        when (filter) {
                            GroceryFilter.ALL -> items
                            GroceryFilter.UNPURCHASED -> items.filter { !it.isChecked }
                            GroceryFilter.PURCHASED -> items.filter { it.isChecked }
                        }
                    val recipeFiltered =
                        when (recipeFilter) {
                            null -> filtered
                            GroceryListBloc.NO_RECIPE_FILTER ->
                                filtered.filter { it.recipeName == null }
                            else -> filtered.filter { it.recipeName == recipeFilter }
                        }
                    val grouped =
                        when (sort) {
                            GrocerySort.AISLE ->
                                recipeFiltered
                                    .groupBy { it.category }
                                    .entries
                                    .sortedBy { it.key.ordinal }
                                    .map { (category, categoryItems) ->
                                        GroceryGroup(
                                            category = category,
                                            items = categoryItems.toImmutableList(),
                                        )
                                    }
                            GrocerySort.ALPHABETICAL ->
                                listOf(
                                        GroceryGroup(
                                            category =
                                                recipeFiltered.firstOrNull()?.category
                                                    ?: com.plusmobileapps.chefmate.grocery.data
                                                        .GroceryCategory
                                                        .OTHER,
                                            items =
                                                recipeFiltered
                                                    .sortedBy { it.displayName.lowercase() }
                                                    .toImmutableList(),
                                        )
                                    )
                                    .filter { it.items.isNotEmpty() }
                        }
                    Triple(grouped, availableRecipes, hasNoRecipeItems)
                }
                .collect { (grouped, availableRecipes, hasNoRecipeItems) ->
                    _state.update {
                        it.copy(
                            groupedItems = grouped,
                            availableRecipes = availableRecipes,
                            hasNoRecipeItems = hasNoRecipeItems,
                        )
                    }
                }
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

    fun onApplySortAndFilter(
        sort: GrocerySort,
        filter: GroceryFilter,
        recipeFilter: String? = null,
    ) {
        _sort.value = sort
        _filter.value = filter
        _recipeFilter.value = recipeFilter
        sortPref = sort.name
        filterPref = filter.name
        _state.update { it.copy(sort = sort, filter = filter, recipeFilter = recipeFilter) }
    }

    /**
     * Resets the purchase and recipe filters to their defaults while preserving the chosen sort
     * order (sort never removes items, so it can't be the cause of an empty filtered list).
     */
    fun onClearFiltersClicked() {
        _filter.value = GroceryFilter.ALL
        _recipeFilter.value = null
        filterPref = GroceryFilter.ALL.name
        _state.update { it.copy(filter = GroceryFilter.ALL, recipeFilter = null) }
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
        val sort: GrocerySort = GrocerySort.AISLE,
        val filter: GroceryFilter = GroceryFilter.ALL,
        val recipeFilter: String? = null,
        val availableRecipes: List<String> = emptyList(),
        val hasNoRecipeItems: Boolean = false,
        val isSyncing: Boolean = false,
        val lists: List<GroceryListModel> = emptyList(),
        val selectedList: GroceryListModel? = null,
        val showCreateListDialog: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val showListSelector: Boolean = false,
    )

    companion object {
        private const val KEY_SORT = "grocery_list_sort"
        private const val KEY_FILTER = "grocery_list_filter"
    }
}
