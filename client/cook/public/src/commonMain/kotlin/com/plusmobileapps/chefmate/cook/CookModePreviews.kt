@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.cook

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.SyncStatus
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow

private val sampleRecipe =
    Recipe(
        id = 1L,
        title = "Pasta Carbonara",
        description = "A classic Roman pasta with eggs, cheese, and cured pork.",
        ingredients =
            "200g spaghetti\n100g guanciale\n2 large eggs\n50g pecorino romano\nFreshly cracked black pepper\nSalt",
        directions =
            "Bring a large pot of salted water to a boil and cook pasta until al dente.\n" +
                "Meanwhile, render the guanciale in a wide pan over medium heat until crisp.\n" +
                "Whisk eggs and pecorino in a bowl with plenty of black pepper.\n" +
                "Drain pasta (reserving a cup of water) and add to the pan off the heat.\n" +
                "Pour in the egg mixture and toss vigorously, loosening with pasta water until silky.",
        imageUrl = null,
        sourceUrl = null,
        servings = 2,
        prepTime = 10,
        cookTime = 15,
        totalTime = 25,
        calories = 620,
        starRating = 5,
        isFavorite = true,
        syncStatus = SyncStatus.SYNCED,
        createdAt = Instant.DISTANT_PAST,
        updatedAt = Instant.DISTANT_PAST,
    )

val previewWhatsCookingBloc: WhatsCookingBloc =
    object : WhatsCookingBloc {
        override val state =
            MutableStateFlow(
                WhatsCookingBloc.Model(
                    recipes =
                        listOf(
                            WhatsCookingBloc.Model.Item(1L, "Pasta Carbonara", null),
                            WhatsCookingBloc.Model.Item(2L, "Caesar Salad", null),
                        )
                )
            )

        override fun onRecipeClicked(recipeId: Long) = Unit

        override fun onSelectModeToggled() = Unit

        override fun onSelectionToggled(recipeId: Long) = Unit

        override fun onDeleteSelectedClicked() = Unit

        override fun onCloseClicked() = Unit
    }

private fun cookBloc(model: CookModeBloc.Model): CookModeBloc =
    object : CookModeBloc {
        override val state = MutableStateFlow(model)
        override val whatsCookingBloc = previewWhatsCookingBloc

        override fun onCloseClicked() = Unit

        override fun onRecipeChipClicked(recipeId: Long) = Unit

        override fun onLayoutToggled() = Unit

        override fun onBackClicked() = Unit
    }

private val activeSessionsSample =
    listOf(
        CookModeBloc.Model.Chip(1L, "Pasta Carbonara", isActive = true),
        CookModeBloc.Model.Chip(2L, "Caesar Salad", isActive = false),
    )

val previewCookBlocStacked: CookModeBloc =
    cookBloc(
        CookModeBloc.Model(
            isLoading = false,
            activeRecipe = sampleRecipe,
            activeSessions = activeSessionsSample,
            layoutMode = CookModeBloc.LayoutMode.Stacked,
        )
    )

val previewCookBlocSplit: CookModeBloc =
    cookBloc(previewCookBlocStacked.state.value.copy(layoutMode = CookModeBloc.LayoutMode.Split))

val previewCookBlocLoading: CookModeBloc = cookBloc(CookModeBloc.Model(isLoading = true))

val previewCookBlocEmpty: CookModeBloc = cookBloc(CookModeBloc.Model(isLoading = false))

@Preview(showBackground = true, heightDp = 1100)
@Composable
internal fun CookModeStackedPreview() {
    ChefMateTheme { CookModeScreen(bloc = previewCookBlocStacked) }
}

@Preview(showBackground = true, heightDp = 1100, widthDp = 800)
@Composable
internal fun CookModeSplitPreview() {
    ChefMateTheme { CookModeScreen(bloc = previewCookBlocSplit) }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
internal fun CookModeLoadingPreview() {
    ChefMateTheme { CookModeScreen(bloc = previewCookBlocLoading) }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
internal fun CookModeEmptyPreview() {
    ChefMateTheme { CookModeScreen(bloc = previewCookBlocEmpty) }
}
