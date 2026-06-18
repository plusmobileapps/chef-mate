package com.plusmobileapps.chefmate.recipe.core.addmeal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface RecipePickerBloc : ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        RecipePickerScreen(bloc = this, modifier = modifier)
    }

    fun onSearchQueryChanged(query: String)

    fun onRecipeSelected(item: RecipePickerItem)

    data class RecipePickerItem(
        val id: Long,
        val title: String,
        val imageUrl: String?,
        /** Names of the recipe book(s) this recipe belongs to, sorted alphabetically. */
        val bookNames: ImmutableList<String> = persistentListOf(),
    )

    data class Model(
        val recipes: ImmutableList<RecipePickerItem> = persistentListOf(),
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
