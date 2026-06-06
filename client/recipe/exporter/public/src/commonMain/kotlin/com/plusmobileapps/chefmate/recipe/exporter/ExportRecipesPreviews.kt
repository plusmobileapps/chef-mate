package com.plusmobileapps.chefmate.recipe.exporter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc.ExportItem
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc.Model
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc.Phase
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow

private fun exportRecipesBloc(model: Model): ExportRecipesBloc =
    object : ExportRecipesBloc {
        override val backHandler: BackHandler = BackDispatcher()
        override val state = MutableStateFlow(model)

        override fun onRecipeToggled(id: String) = Unit

        override fun onToggleSelectAll() = Unit

        override fun onExportClicked() = Unit

        override fun onSaveCompleted(saved: Boolean) = Unit

        override fun onStartOver() = Unit

        override fun onBack() = Unit

        @Composable
        override fun Content(modifier: Modifier) {
            ExportRecipesScreen(bloc = this, modifier = modifier)
        }
    }

private val sampleItems =
    persistentListOf(
        ExportItem(
            id = "1",
            title = "Marcella Hazan Bolognese",
            subtitle = FixedString("15 ingredients · 5 steps"),
            imageUrl = null,
            selected = true,
        ),
        ExportItem(
            id = "2",
            title = "Meatloaf",
            subtitle = FixedString("16 ingredients · 7 steps"),
            imageUrl = null,
            selected = true,
        ),
        ExportItem(
            id = "3",
            title = "How to Cook Tempeh",
            subtitle = FixedString("5 ingredients · 3 steps"),
            imageUrl = null,
            selected = false,
        ),
    )

val previewExportRecipesLoadingBloc: ExportRecipesBloc = exportRecipesBloc(Model(Phase.Loading))

val previewExportRecipesEmptyBloc: ExportRecipesBloc = exportRecipesBloc(Model(Phase.Empty))

val previewExportRecipesReviewBloc: ExportRecipesBloc =
    exportRecipesBloc(Model(Phase.Review(recipes = sampleItems)))

val previewExportRecipesReviewNoSelectionBloc: ExportRecipesBloc =
    exportRecipesBloc(
        Model(
            Phase.Review(recipes = sampleItems.map { it.copy(selected = false) }.toImmutableList())
        )
    )

val previewExportRecipesExportingBloc: ExportRecipesBloc =
    exportRecipesBloc(Model(Phase.Review(recipes = sampleItems, isExporting = true)))

val previewExportRecipesDoneBloc: ExportRecipesBloc =
    exportRecipesBloc(Model(Phase.Done(exportedCount = 2)))

val previewExportRecipesErrorBloc: ExportRecipesBloc =
    exportRecipesBloc(Model(Phase.Error(FixedString("We couldn’t save that archive."))))

@Preview
@Composable
internal fun ExportRecipesReviewPreview() {
    ChefMateTheme { ExportRecipesScreen(bloc = previewExportRecipesReviewBloc) }
}

@Preview
@Composable
internal fun ExportRecipesEmptyPreview() {
    ChefMateTheme { ExportRecipesScreen(bloc = previewExportRecipesEmptyBloc) }
}

@Preview
@Composable
internal fun ExportRecipesDonePreview() {
    ChefMateTheme { ExportRecipesScreen(bloc = previewExportRecipesDoneBloc) }
}

@Preview
@Composable
internal fun ExportRecipesReviewDarkPreview() {
    ChefMateTheme(darkTheme = true) { ExportRecipesScreen(bloc = previewExportRecipesReviewBloc) }
}
