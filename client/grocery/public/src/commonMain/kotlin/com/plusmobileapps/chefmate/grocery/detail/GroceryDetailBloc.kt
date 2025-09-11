package com.plusmobileapps.chefmate.grocery.detail

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import kotlinx.coroutines.flow.StateFlow

interface GroceryDetailBloc : BackClickBloc {

    val models: StateFlow<Model>

    fun onGroceryNameChanged(name: String)

    fun onGroceryCheckedChanged(isChecked: Boolean)

    fun onSaveClicked()

    sealed class Model {
        object Loading : Model()
        data class Loaded(
            val item: GroceryItem
        ) : Model()
    }

    sealed class Output {
        data object Finished : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            id: Long,
            output: Consumer<Output>
        ): GroceryDetailBloc
    }

}