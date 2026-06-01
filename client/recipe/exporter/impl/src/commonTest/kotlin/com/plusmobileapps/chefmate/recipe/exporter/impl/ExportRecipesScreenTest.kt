@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.recipe.exporter.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc.ExportItem
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc.Model
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc.Phase
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesScreen
import com.plusmobileapps.chefmate.recipe.exporter.robots.exportRecipes
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow

class ExportRecipesScreenTest {

    @Test
    fun review_then_export_shows_done() = runComposeUiTest {
        val bloc = StatefulExportBloc()
        setContent { ChefMateTheme { ExportRecipesScreen(bloc = bloc) } }

        exportRecipes()
            .assertRecipeDisplayed("Garlic Noodles")
            .assertRecipeDisplayed("Lentil Curry")
            .toggleRecipe("Lentil Curry")
            .clickExport()
            .assertDoneVisible()
    }

    /** Minimal stateful bloc that moves Review → Done so the robot can drive a full flow. */
    private class StatefulExportBloc : ExportRecipesBloc {
        override val backHandler: BackHandler = BackDispatcher()
        override val state =
            MutableStateFlow(
                Model(Phase.Review(listOf(item("1", "Garlic Noodles"), item("2", "Lentil Curry"))))
            )

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

        override fun onExportClicked() {
            val review = state.value.phase as? Phase.Review ?: return
            state.value = Model(Phase.Done(exportedCount = review.recipes.count { it.selected }))
        }

        override fun onSaveCompleted(saved: Boolean) = Unit

        override fun onStartOver() = Unit

        override fun onBack() = Unit

        @Composable
        override fun Content(modifier: Modifier) {
            ExportRecipesScreen(bloc = this, modifier = modifier)
        }

        private companion object {
            fun item(id: String, title: String) =
                ExportItem(
                    id = id,
                    title = title,
                    subtitle = FixedString("1 ingredient · 1 step"),
                    hasImage = false,
                    selected = true,
                )
        }
    }
}
