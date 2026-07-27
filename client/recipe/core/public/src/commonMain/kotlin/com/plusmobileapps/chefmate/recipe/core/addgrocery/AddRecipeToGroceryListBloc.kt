package com.plusmobileapps.chefmate.recipe.core.addgrocery

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.recipe.data.DEFAULT_INGREDIENT_SCALE
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface AddRecipeToGroceryListBloc : BackClickBloc, ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        AddRecipeToGroceryListScreen(bloc = this, modifier = modifier)
    }

    fun onIngredientToggled(ingredient: Int)

    fun onGroceryListSelected(listId: Long)

    /**
     * Pick a new ingredient scale factor (e.g. `2.0` for 2×). Shares the per-recipe preference with
     * the recipe detail screen and Cook Mode, and is applied to the amounts added to the list.
     */
    fun onScaleChanged(scale: Double)

    fun onSaveClicked()

    data class GroceryListItem(val id: Long, val name: String)

    data class IngredientGroup(val category: GroceryCategory, val items: ImmutableList<ListItem>)

    data class ListItem(
        val id: Int,
        val name: String,
        val displayName: String,
        val quantity: String? = null,
        val isSelected: Boolean,
    )

    data class Model(
        val isLoading: Boolean,
        val isAdding: Boolean,
        val groupedIngredients: ImmutableList<IngredientGroup>,
        val groceryLists: ImmutableList<GroceryListItem> = persistentListOf(),
        val selectedGroceryList: GroceryListItem? = null,
        /** The factor the listed amounts are scaled by (1× = the recipe author's amounts). */
        val ingredientScale: Double = DEFAULT_INGREDIENT_SCALE,
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
