package com.plusmobileapps.chefmate.recipe.core.impl.addmeal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.recipe.core.addmeal.RecipePickerBloc
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow

private fun recipePickerBloc(model: RecipePickerBloc.Model): RecipePickerBloc =
    object : RecipePickerBloc {
        override val state = MutableStateFlow(model)

        override fun onSearchQueryChanged(query: String) = Unit

        override fun onRecipeSelected(item: RecipePickerBloc.RecipePickerItem) = Unit

        @Composable
        override fun Content(modifier: Modifier) =
            RecipePickerScreen(bloc = this, modifier = modifier)
    }

private val sampleRecipes =
    persistentListOf(
        RecipePickerBloc.RecipePickerItem(
            id = 1L,
            title = "Sourdough Pancakes",
            imageUrl = null,
            bookNames = persistentListOf("Breakfast Favorites"),
        ),
        RecipePickerBloc.RecipePickerItem(
            id = 2L,
            title = "Caesar Salad",
            imageUrl = null,
            bookNames = persistentListOf("Weeknight Dinners", "Salads"),
        ),
        // A recipe filed under no named book — should render just its title.
        RecipePickerBloc.RecipePickerItem(id = 3L, title = "Pasta Carbonara", imageUrl = null),
    )

val previewRecipePickerBloc: RecipePickerBloc =
    recipePickerBloc(RecipePickerBloc.Model(recipes = sampleRecipes, isLoading = false))

val previewRecipePickerBlocLoading: RecipePickerBloc =
    recipePickerBloc(RecipePickerBloc.Model(isLoading = true))

@Preview
@Composable
internal fun RecipePickerScreenPreview() {
    ChefMateTheme { previewRecipePickerBloc.Content(Modifier) }
}

@Preview
@Composable
internal fun RecipePickerScreenLoadingPreview() {
    ChefMateTheme { previewRecipePickerBlocLoading.Content(Modifier) }
}
