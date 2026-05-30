@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.recipe.importer.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc.ImportItem
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc.Model
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc.Phase
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesScreen
import com.plusmobileapps.chefmate.recipe.importer.robots.importRecipes
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow

class ImportRecipesScreenTest {

    @Test
    fun review_then_import_shows_done() = runComposeUiTest {
        val bloc = StatefulImportBloc()
        setContent { ChefMateTheme { ImportRecipesScreen(bloc = bloc) } }

        importRecipes()
            .assertRecipeDisplayed("Garlic Noodles")
            .assertRecipeDisplayed("Lentil Curry")
            .toggleRecipe("Lentil Curry")
            .clickImport()
            .assertDoneVisible()
    }

    /** Minimal stateful bloc that moves Review → Done so the robot can drive a full flow. */
    private class StatefulImportBloc : ImportRecipesBloc {
        override val backHandler: BackHandler = BackDispatcher()
        override val state =
            MutableStateFlow(
                Model(Phase.Review(listOf(item("0", "Garlic Noodles"), item("1", "Lentil Curry"))))
            )

        override fun onArchiveSelected(bytes: ByteArray, fileName: String) = Unit

        override fun onRecipeToggled(id: String) {
            val review = state.value.phase as? Phase.Review ?: return
            state.value =
                Model(
                    review.copy(
                        recipes =
                            review.recipes.map {
                                if (it.id == id) it.copy(selected = !it.selected) else it
                            }
                    )
                )
        }

        override fun onToggleSelectAll() = Unit

        override fun onImportClicked() {
            val review = state.value.phase as? Phase.Review ?: return
            state.value = Model(Phase.Done(importedCount = review.recipes.count { it.selected }))
        }

        override fun onStartOver() = Unit

        override fun onBack() = Unit

        @Composable
        override fun Content(modifier: Modifier) {
            ImportRecipesScreen(bloc = this, modifier = modifier)
        }

        private companion object {
            fun item(id: String, title: String) =
                ImportItem(
                    id = id,
                    title = title,
                    subtitle = FixedString("1 ingredient · 1 step"),
                    hasImage = false,
                    selected = true,
                )
        }
    }
}
