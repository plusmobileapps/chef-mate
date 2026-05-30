package com.plusmobileapps.chefmate.recipe.importer

import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.coroutines.flow.StateFlow

interface ImportRecipesBloc : BackHandlerOwner, BlocScreen {
    val state: StateFlow<Model>

    /** Called by the screen once the platform picker returns the chosen archive. */
    fun onArchiveSelected(bytes: ByteArray, fileName: String)

    fun onRecipeToggled(id: String)

    fun onToggleSelectAll()

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
        data class Review(val recipes: List<ImportItem>, val isImporting: Boolean = false) : Phase

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
