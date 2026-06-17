package com.plusmobileapps.chefmate.recipe.importer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipebook.data.RecipeBook
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

interface ImportRecipesBloc : BackHandlerOwner, ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        ImportRecipesScreen(bloc = this, modifier = modifier)
    }

    /** All of the user's recipe books, offered as a multi-select target for the import. */
    val recipeBooks: StateFlow<List<RecipeBook>>

    /** Local ids of the books the imported recipes will be filed under. */
    val selectedBookIds: StateFlow<Set<Long>>

    /** Called by the screen once the platform picker returns the chosen archive. */
    fun onArchiveSelected(bytes: ByteArray, fileName: String)

    fun onRecipeToggled(id: String)

    fun onToggleSelectAll()

    /** Toggles whether the imported recipes are filed under the book with local id [bookId]. */
    fun onToggleBook(bookId: Long)

    fun onImportClicked()

    /** Resets back to the empty state so the user can choose a different archive. */
    fun onStartOver()

    fun onBack()

    data class Model(val phase: Phase = Phase.Empty)

    sealed interface Phase {
        /** No archive chosen yet — prompt the user to pick one. */
        data object Empty : Phase

        /** Decompressing and parsing the archive. */
        data object Parsing : Phase

        /** Parsed recipes are ready for the user to review and select. */
        data class Review(
            val recipes: ImmutableList<ImportItem>,
            val isImporting: Boolean = false,
        ) : Phase

        /** Import finished successfully. */
        data class Done(val importedCount: Int) : Phase

        /** Parsing or importing failed. */
        data class Error(val message: TextData) : Phase
    }

    data class ImportItem(
        val id: String,
        val title: String,
        val subtitle: TextData,
        val hasImage: Boolean,
        val selected: Boolean,
    )

    sealed class Output {
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): ImportRecipesBloc
    }
}
