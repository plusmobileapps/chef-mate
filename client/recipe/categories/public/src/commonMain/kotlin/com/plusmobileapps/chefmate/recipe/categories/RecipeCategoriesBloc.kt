package com.plusmobileapps.chefmate.recipe.categories

import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface RecipeCategoriesBloc : BackHandlerOwner, BackClickBloc, BlocScreen {
    val state: StateFlow<Model>

    fun onCategoryClicked(item: CategoryItem)

    fun onCategoryLongClicked(item: CategoryItem)

    fun onSelectModeClicked()

    fun onCancelSelection()

    fun onCreateClicked()

    fun onCreateCancelled()

    fun onCreateTextChanged(text: String)

    fun onCreateSubmitted()

    fun onRenameRequested(item: CategoryItem)

    fun onRenameSubmitted(id: Long, newName: String)

    fun onRenameDismissed()

    fun onDeleteRequested(item: CategoryItem)

    fun onDeleteConfirmed()

    fun onDeleteDismissed()

    fun onBulkDeleteRequested()

    fun onBulkDeleteConfirmed()

    fun onBulkDeleteDismissed()

    data class Model(
        val categories: ImmutableList<CategoryItem> = persistentListOf(),
        val isLoading: Boolean = true,
        val selectionMode: Boolean = false,
        val selectedIds: Set<Long> = emptySet(),
        val createState: CreateState = CreateState.Hidden,
        val dialog: DialogState = DialogState.None,
    ) {
        val selectedCount: Int
            get() = selectedIds.size
    }

    /**
     * One row in the management screen. [id] is `0L` for built-in presets that have not been
     * materialized yet — those rows are read-only and cannot enter selection mode or appear in the
     * dialog targets.
     */
    data class CategoryItem(
        val id: Long,
        val name: String,
        val builtinId: String?,
        val recipeCount: Int,
    ) {
        val isBuiltin: Boolean
            get() = builtinId != null

        /** Whether the user can rename / delete / multi-select this row. */
        val isEditable: Boolean
            get() = !isBuiltin && id != 0L
    }

    sealed class CreateState {
        data object Hidden : CreateState()

        data class Editing(val text: String) : CreateState()
    }

    sealed class DialogState {
        data object None : DialogState()

        data class Rename(val target: CategoryItem) : DialogState()

        data class Delete(val target: CategoryItem) : DialogState()

        data class BulkDelete(val count: Int) : DialogState()
    }

    sealed class Output {
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): RecipeCategoriesBloc
    }
}
