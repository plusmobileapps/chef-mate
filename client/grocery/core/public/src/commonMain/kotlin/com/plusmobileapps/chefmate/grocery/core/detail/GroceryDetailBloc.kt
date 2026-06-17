package com.plusmobileapps.chefmate.grocery.core.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.coroutines.flow.StateFlow

interface GroceryDetailBloc : BackClickBloc, ComposeScreen {
    val models: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        GroceryDetailSheetContent(bloc = this, modifier = modifier)
    }

    fun onGroceryNameChanged(name: String)

    fun onGroceryQuantityChanged(quantity: String)

    fun onGroceryCheckedChanged(isChecked: Boolean)

    fun onAisleChanged(category: GroceryCategory)

    fun onSaveClicked()

    sealed class Model {
        object Loading : Model()

        data class Loaded(val item: GroceryItem) : Model()
    }

    sealed class Output {
        data object Finished : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, id: Long, output: Consumer<Output>): GroceryDetailBloc
    }
}
