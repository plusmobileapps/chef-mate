package com.plusmobileapps.chefmate.recipe.exporter

import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.coroutines.flow.StateFlow

interface ExportRecipesBloc : BackHandlerOwner, BlocScreen {
    val state: StateFlow<Model>

    fun onRecipeToggled(id: String)

    fun onToggleSelectAll()

    fun onExportClicked()

    /** Called by the screen once the platform save dialog finishes. */
    fun onSaveCompleted(saved: Boolean)

    fun onStartOver()

    fun onBack()

    data class Model(val phase: Phase = Phase.Loading, val pendingSave: PendingSave? = null)

    sealed interface Phase {
        /** Initial state while we read recipes from the local cache. */
        data object Loading : Phase

        /** Repository returned no recipes, so there is nothing to export. */
        data object Empty : Phase

        /** Recipes are ready for the user to select and export. */
        data class Review(val recipes: List<ExportItem>, val isExporting: Boolean = false) : Phase

        /** Export pipeline completed end-to-end (zip generated + saved by the user). */
        data class Done(val exportedCount: Int) : Phase

        /** Building or saving the archive failed. */
        data class Error(val message: TextData) : Phase
    }

    data class ExportItem(
        val id: String,
        val title: String,
        val subtitle: TextData,
        val imageUrl: String?,
        val selected: Boolean,
    )

    /**
     * A built archive waiting to be handed to the platform save dialog. [token] changes on every
     * generation so the screen can re-trigger its launcher even if the file name + bytes look
     * identical to a previous attempt the user cancelled.
     */
    class PendingSave(val token: Long, val fileName: String, val archive: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PendingSave) return false
            return token == other.token &&
                fileName == other.fileName &&
                archive.contentEquals(other.archive)
        }

        override fun hashCode(): Int {
            var result = token.hashCode()
            result = 31 * result + fileName.hashCode()
            result = 31 * result + archive.contentHashCode()
            return result
        }
    }

    sealed class Output {
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): ExportRecipesBloc
    }
}
