package com.plusmobileapps.chefmate.meal.core.recipepicker

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import kotlinx.coroutines.flow.StateFlow

interface RecipePickerBloc {
    val state: StateFlow<Model>

    fun onSearchQueryChanged(query: String)

    fun onRecipeSelected(item: RecipePickerItem)

    data class RecipePickerItem(val id: Long, val title: String, val imageUrl: String?)

    data class Model(
        val recipes: List<RecipePickerItem> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = true,
    )

    sealed class Output {
        data class RecipeSelected(val recipeId: Long) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): RecipePickerBloc
    }
}
