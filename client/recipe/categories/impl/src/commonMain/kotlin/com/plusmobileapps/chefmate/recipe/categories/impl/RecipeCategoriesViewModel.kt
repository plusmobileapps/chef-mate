package com.plusmobileapps.chefmate.recipe.categories.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc.CategoryItem
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc.CreateState
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc.DialogState
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc.Model
import com.plusmobileapps.chefmate.recipe.data.CategoryRepository
import com.plusmobileapps.chefmate.recipe.data.CategoryWithCount
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class RecipeCategoriesViewModel(
    @Main mainContext: CoroutineContext,
    private val categoryRepository: CategoryRepository,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(Model())
    val state: StateFlow<Model> = _state.asStateFlow()

    init {
        scope.launch {
            categoryRepository.observeCategoriesWithCounts().collect { items ->
                _state.update { current ->
                    val mapped = items.map { it.toCategoryItem() }
                    // Drop selections for rows that no longer exist (e.g. after a bulk delete).
                    val survivingIds = mapped.map { it.id }.toSet()
                    val cleanedSelection = current.selectedIds intersect survivingIds
                    current.copy(
                        categories = mapped,
                        isLoading = false,
                        selectedIds = cleanedSelection,
                    )
                }
            }
        }
    }

    fun toggleSelection(item: CategoryItem) {
        if (!item.isEditable) return
        _state.update { current ->
            val nextSelection =
                if (item.id in current.selectedIds) current.selectedIds - item.id
                else current.selectedIds + item.id
            // Stay in selection mode even at 0 selected — the user explicitly entered it via
            // the header button or long-press, and exits it via the close icon.
            current.copy(selectedIds = nextSelection)
        }
    }

    fun enterSelectionWith(item: CategoryItem) {
        if (!item.isEditable) return
        _state.update { current ->
            current.copy(selectionMode = true, selectedIds = current.selectedIds + item.id)
        }
    }

    fun enterSelectionMode() {
        _state.update { it.copy(selectionMode = true) }
    }

    fun cancelSelection() {
        _state.update { it.copy(selectionMode = false, selectedIds = emptySet()) }
    }

    fun openCreateField() {
        _state.update { current ->
            if (current.createState is CreateState.Editing) current
            else current.copy(createState = CreateState.Editing(text = ""))
        }
    }

    fun closeCreateField() {
        _state.update { it.copy(createState = CreateState.Hidden) }
    }

    fun updateCreateText(text: String) {
        _state.update { current ->
            val editing = current.createState as? CreateState.Editing ?: return@update current
            current.copy(createState = editing.copy(text = text))
        }
    }

    fun submitCreate() {
        val name = (_state.value.createState as? CreateState.Editing)?.text?.trim().orEmpty()
        if (name.isBlank()) return
        _state.update { it.copy(createState = CreateState.Hidden) }
        scope.launch { categoryRepository.createUserCategory(name) }
    }

    fun showRenameDialog(item: CategoryItem) {
        if (!item.isEditable) return
        _state.update { it.copy(dialog = DialogState.Rename(item)) }
    }

    fun submitRename(id: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        _state.update { it.copy(dialog = DialogState.None) }
        scope.launch { categoryRepository.renameCategory(id, trimmed) }
    }

    fun showDeleteDialog(item: CategoryItem) {
        if (!item.isEditable) return
        _state.update { it.copy(dialog = DialogState.Delete(item)) }
    }

    fun confirmDelete() {
        val target = (_state.value.dialog as? DialogState.Delete)?.target ?: return
        _state.update { it.copy(dialog = DialogState.None) }
        scope.launch { categoryRepository.deleteCategory(target.id) }
    }

    fun showBulkDeleteDialog() {
        val count = _state.value.selectedIds.size
        if (count == 0) return
        _state.update { it.copy(dialog = DialogState.BulkDelete(count = count)) }
    }

    fun confirmBulkDelete() {
        val ids = _state.value.selectedIds.toList()
        if (ids.isEmpty()) return
        _state.update {
            it.copy(dialog = DialogState.None, selectedIds = emptySet(), selectionMode = false)
        }
        scope.launch { ids.forEach { id -> categoryRepository.deleteCategory(id) } }
    }

    fun dismissDialog() {
        _state.update { it.copy(dialog = DialogState.None) }
    }

    private fun CategoryWithCount.toCategoryItem(): CategoryItem =
        CategoryItem(
            id = category.id,
            name = category.name,
            builtinId = category.builtinId,
            recipeCount = recipeCount,
        )
}
