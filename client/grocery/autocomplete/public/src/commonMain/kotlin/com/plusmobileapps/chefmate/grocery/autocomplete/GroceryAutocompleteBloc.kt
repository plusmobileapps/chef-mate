package com.plusmobileapps.chefmate.grocery.autocomplete

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface GroceryAutocompleteBloc : BackHandlerOwner, BackClickBloc, ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        GroceryAutocompleteSettingsScreen(bloc = this, modifier = modifier)
    }

    fun onCreateClicked()

    fun onCreateCancelled()

    fun onCreateTextChanged(text: String)

    fun onCreateSubmitted()

    fun onDeleteRequested(item: Item)

    fun onDeleteConfirmed()

    fun onDeleteDismissed()

    data class Model(
        val userItems: ImmutableList<Item> = persistentListOf(),
        /**
         * Read-only built-in suggestions from IngredientParser, minus any the user has re-added.
         */
        val defaults: ImmutableList<String> = persistentListOf(),
        val isLoading: Boolean = true,
        val createState: CreateState = CreateState.Hidden,
        val dialog: DialogState = DialogState.None,
    )

    /** One user-defined autocomplete entry. */
    data class Item(val id: Long, val name: String)

    sealed class CreateState {
        data object Hidden : CreateState()

        data class Editing(val text: String) : CreateState()
    }

    sealed class DialogState {
        data object None : DialogState()

        data class Delete(val target: Item) : DialogState()
    }

    sealed class Output {
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): GroceryAutocompleteBloc
    }
}
