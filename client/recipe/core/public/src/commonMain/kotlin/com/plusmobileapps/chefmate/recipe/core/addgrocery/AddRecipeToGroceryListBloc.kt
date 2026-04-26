package com.plusmobileapps.chefmate.recipe.core.addgrocery

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import kotlinx.coroutines.flow.StateFlow

interface AddRecipeToGroceryListBloc : BackClickBloc {
    val state: StateFlow<Model>

    fun onIngredientToggled(ingredient: Int)

    fun onGroceryListSelected(listId: Long)

    fun onSaveClicked()

    data class GroceryListItem(val id: Long, val name: String)

    data class IngredientGroup(val category: GroceryCategory, val items: List<ListItem>)

    data class ListItem(val id: Int, val name: String, val isSelected: Boolean)

    data class Model(
        val isLoading: Boolean,
        val isAdding: Boolean,
        val groupedIngredients: List<IngredientGroup>,
        val groceryLists: List<GroceryListItem> = emptyList(),
        val selectedGroceryList: GroceryListItem? = null,
    ) {
        val hasSelectedIngredients: Boolean
            get() = groupedIngredients.any { group -> group.items.any { it.isSelected } }

        val isEmpty: Boolean
            get() = groupedIngredients.isEmpty()
    }

    sealed class Output {
        data object Finished : Output()

        data object Added : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            recipeId: Long,
            output: Consumer<Output>,
        ): AddRecipeToGroceryListBloc
    }
}
