package com.plusmobileapps.chefmate.grocery.autocomplete.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteBloc.CreateState
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteBloc.DialogState
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteBloc.Item
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteBloc.Model
import com.plusmobileapps.chefmate.grocery.data.GroceryAutocompleteRepository
import com.plusmobileapps.chefmate.grocery.data.IngredientParser
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class GroceryAutocompleteViewModel(
    @Main mainContext: CoroutineContext,
    private val repository: GroceryAutocompleteRepository,
) : ViewModel(mainContext) {

    // Built-in vocabulary is static; compute once and filter out anything the user re-adds.
    private val allDefaults = IngredientParser.allSuggestionNames()

    private val _state = MutableStateFlow(Model(defaults = allDefaults.toImmutableList()))
    val state: StateFlow<Model> = _state.asStateFlow()

    init {
        scope.launch {
            repository.observeItems().collect { items ->
                val userNames = items.mapTo(mutableSetOf()) { it.name.lowercase() }
                _state.update { current ->
                    current.copy(
                        userItems =
                            items.map { Item(id = it.id, name = it.name) }.toImmutableList(),
                        defaults =
                            allDefaults.filter { it.lowercase() !in userNames }.toImmutableList(),
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun openCreateField() {
        _state.update { current ->
            if (current.createState is CreateState.Editing) current
            else current.copy(createState = CreateState.Editing(text = ""))
        }
    }

    fun closeCreateField() {
        _state.update { it.copy(createState = CreateState.Hidden) }
    }

    fun updateCreateText(text: String) {
        _state.update { current ->
            val editing = current.createState as? CreateState.Editing ?: return@update current
            current.copy(createState = editing.copy(text = text))
        }
    }

    fun submitCreate() {
        val name = (_state.value.createState as? CreateState.Editing)?.text?.trim().orEmpty()
        if (name.isBlank()) return
        _state.update { it.copy(createState = CreateState.Hidden) }
        scope.launch { repository.addItem(name) }
    }

    fun showDeleteDialog(item: Item) {
        _state.update { it.copy(dialog = DialogState.Delete(item)) }
    }

    fun confirmDelete() {
        val target = (_state.value.dialog as? DialogState.Delete)?.target ?: return
        _state.update { it.copy(dialog = DialogState.None) }
        scope.launch { repository.deleteItem(target.id) }
    }

    fun dismissDialog() {
        _state.update { it.copy(dialog = DialogState.None) }
    }
}
