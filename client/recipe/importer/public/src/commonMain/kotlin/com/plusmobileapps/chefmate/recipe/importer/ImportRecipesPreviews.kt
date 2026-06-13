package com.plusmobileapps.chefmate.recipe.importer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc.ImportItem
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc.Model
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc.Phase
import com.plusmobileapps.chefmate.recipebook.data.RecipeBook
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow

private fun importRecipesBloc(
    model: Model,
    selectedBookIds: Set<Long> = setOf(1L),
): ImportRecipesBloc =
    object : ImportRecipesBloc {
        override val backHandler: BackHandler = BackDispatcher()
        override val state = MutableStateFlow(model)
        override val recipeBooks = MutableStateFlow(RecipeBook.Samples)
        override val selectedBookIds = MutableStateFlow(selectedBookIds)

        override fun onArchiveSelected(bytes: ByteArray, fileName: String) = Unit

        override fun onRecipeToggled(id: String) = Unit

        override fun onToggleSelectAll() = Unit

        override fun onToggleBook(bookId: Long) = Unit

        override fun onImportClicked() = Unit

        override fun onStartOver() = Unit

        override fun onBack() = Unit

        @Composable
        override fun Content(modifier: Modifier) {
            ImportRecipesScreen(bloc = this, modifier = modifier)
        }
    }

private val sampleItems =
    persistentListOf(
        ImportItem(
            id = "0",
            title = "Garlic Chilli Noodles",
            subtitle = FixedString("15 ingredients · 4 steps"),
            hasImage = true,
            selected = true,
        ),
        ImportItem(
            id = "1",
            title = "Lentil Curry + Potatoes",
            subtitle = FixedString("11 ingredients · 6 steps"),
            hasImage = true,
            selected = true,
        ),
        ImportItem(
            id = "2",
            title = "Lactation Coronado Cookies",
            subtitle = FixedString("9 ingredients · 5 steps"),
            hasImage = false,
            selected = false,
        ),
    )

val previewImportRecipesEmptyBloc: ImportRecipesBloc = importRecipesBloc(Model(Phase.Empty))

val previewImportRecipesParsingBloc: ImportRecipesBloc = importRecipesBloc(Model(Phase.Parsing))

val previewImportRecipesReviewBloc: ImportRecipesBloc =
    importRecipesBloc(Model(Phase.Review(recipes = sampleItems)))

val previewImportRecipesImportingBloc: ImportRecipesBloc =
    importRecipesBloc(Model(Phase.Review(recipes = sampleItems, isImporting = true)))

/** Review with no book selected — the required-book hint shows and import is disabled. */
val previewImportRecipesNoBookBloc: ImportRecipesBloc =
    importRecipesBloc(Model(Phase.Review(recipes = sampleItems)), selectedBookIds = emptySet())

val previewImportRecipesDoneBloc: ImportRecipesBloc =
    importRecipesBloc(Model(Phase.Done(importedCount = 2)))

val previewImportRecipesErrorBloc: ImportRecipesBloc =
    importRecipesBloc(Model(Phase.Error(FixedString("No recipes were found in that archive."))))

@Preview
@Composable
internal fun ImportRecipesEmptyPreview() {
    ChefMateTheme { ImportRecipesScreen(bloc = previewImportRecipesEmptyBloc) }
}

@Preview
@Composable
internal fun ImportRecipesReviewPreview() {
    ChefMateTheme { ImportRecipesScreen(bloc = previewImportRecipesReviewBloc) }
}

@Preview
@Composable
internal fun ImportRecipesNoBookPreview() {
    ChefMateTheme { ImportRecipesScreen(bloc = previewImportRecipesNoBookBloc) }
}

@Preview
@Composable
internal fun ImportRecipesDonePreview() {
    ChefMateTheme { ImportRecipesScreen(bloc = previewImportRecipesDoneBloc) }
}

@Preview
@Composable
internal fun ImportRecipesReviewDarkPreview() {
    ChefMateTheme(darkTheme = true) { ImportRecipesScreen(bloc = previewImportRecipesReviewBloc) }
}
