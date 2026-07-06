package com.plusmobileapps.chefmate.grocery.autocomplete

import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteBloc.CreateState
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteBloc.Item
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteBloc.Model
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow

private fun bloc(model: Model): GroceryAutocompleteBloc =
    object : GroceryAutocompleteBloc {
        override val backHandler: BackHandler = BackDispatcher()
        override val state = MutableStateFlow(model)

        override fun onBackClicked() = Unit

        override fun onCreateClicked() = Unit

        override fun onCreateCancelled() = Unit

        override fun onCreateTextChanged(text: String) = Unit

        override fun onCreateSubmitted() = Unit

        override fun onDeleteRequested(item: Item) = Unit

        override fun onDeleteConfirmed() = Unit

        override fun onDeleteDismissed() = Unit
    }

private val sampleUserItems =
    persistentListOf(
        Item(id = 1L, name = "Cold brew concentrate"),
        Item(id = 2L, name = "Oat milk"),
        Item(id = 3L, name = "Sourdough starter"),
    )

private val sampleDefaults =
    persistentListOf("Apple", "Banana", "Bread", "Butter", "Chicken", "Eggs", "Milk", "Rice")

val previewGroceryAutocompleteBloc: GroceryAutocompleteBloc =
    bloc(Model(userItems = sampleUserItems, defaults = sampleDefaults, isLoading = false))

val previewGroceryAutocompleteBlocEmptyUser: GroceryAutocompleteBloc =
    bloc(Model(userItems = persistentListOf(), defaults = sampleDefaults, isLoading = false))

val previewGroceryAutocompleteBlocCreating: GroceryAutocompleteBloc =
    bloc(
        Model(
            userItems = sampleUserItems,
            defaults = sampleDefaults,
            isLoading = false,
            createState = CreateState.Editing(text = "Kimchi"),
        )
    )

val previewGroceryAutocompleteBlocDeleteDialog: GroceryAutocompleteBloc =
    bloc(
        Model(
            userItems = sampleUserItems,
            defaults = sampleDefaults,
            isLoading = false,
            dialog = GroceryAutocompleteBloc.DialogState.Delete(sampleUserItems.first()),
        )
    )
