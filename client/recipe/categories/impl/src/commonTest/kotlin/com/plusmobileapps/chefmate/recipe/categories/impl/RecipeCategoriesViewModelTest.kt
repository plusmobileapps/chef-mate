@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.recipe.categories.impl

import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc.CreateState
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc.DialogState
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.testing.FakeCategoryRepository
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class RecipeCategoriesViewModelTest {

    private val categories =
        MutableStateFlow(
            listOf(
                Category(id = 1L, name = "Family", builtinId = null),
                Category(id = 2L, name = "Weeknight", builtinId = null),
            )
        )
    private val recipeCounts = MutableStateFlow<Map<Long, Int>>(mapOf(1L to 5, 2L to 2))
    private val repository = FakeCategoryRepository(categories, recipeCounts)
    private val mainContext = UnconfinedTestDispatcher()

    private fun createViewModel() = RecipeCategoriesViewModel(mainContext, repository)

    @Test
    fun loads_user_categories_and_synthesizes_missing_builtins() {
        val vm = createViewModel()
        val items = vm.state.value.categories
        items.find { it.name == "Family" }?.recipeCount shouldBe 5
        items.find { it.name == "Weeknight" }?.recipeCount shouldBe 2
        // Every built-in not in `categories` is synthesized with count 0.
        BuiltinCategory.entries.forEach { builtin ->
            val synthetic = items.find { it.builtinId == builtin.id }
            synthetic?.recipeCount shouldBe 0
            synthetic?.id shouldBe 0L
        }
        vm.state.value.isLoading shouldBe false
    }

    @Test
    fun long_press_on_user_row_enters_selection_mode_with_that_row_selected() {
        val vm = createViewModel()
        val family = vm.state.value.categories.first { it.name == "Family" }
        vm.enterSelectionWith(family)
        vm.state.value.selectionMode shouldBe true
        vm.state.value.selectedIds shouldBe setOf(family.id)
    }

    @Test
    fun enter_selection_mode_via_header_button_starts_with_no_selection() {
        val vm = createViewModel()
        vm.enterSelectionMode()
        vm.state.value.selectionMode shouldBe true
        vm.state.value.selectedIds shouldBe emptySet()
    }

    @Test
    fun deselecting_last_item_keeps_selection_mode_active() {
        val vm = createViewModel()
        val family = vm.state.value.categories.first { it.name == "Family" }
        vm.enterSelectionWith(family)
        vm.toggleSelection(family)
        // Header button stays available; explicit cancel is required to exit selection.
        vm.state.value.selectionMode shouldBe true
        vm.state.value.selectedIds shouldBe emptySet()
    }

    @Test
    fun long_press_on_builtin_row_is_a_no_op() {
        val vm = createViewModel()
        val breakfast =
            vm.state.value.categories.first { it.builtinId == BuiltinCategory.BREAKFAST.id }
        vm.enterSelectionWith(breakfast)
        vm.state.value.selectionMode shouldBe false
        vm.state.value.selectedIds shouldBe emptySet()
    }

    @Test
    fun toggle_selection_adds_then_removes_id() {
        val vm = createViewModel()
        val family = vm.state.value.categories.first { it.name == "Family" }
        val weeknight = vm.state.value.categories.first { it.name == "Weeknight" }
        vm.toggleSelection(family)
        vm.toggleSelection(weeknight)
        vm.state.value.selectedIds shouldBe setOf(family.id, weeknight.id)
        vm.toggleSelection(family)
        vm.state.value.selectedIds shouldBe setOf(weeknight.id)
    }

    @Test
    fun cancel_selection_clears_state() {
        val vm = createViewModel()
        val family = vm.state.value.categories.first { it.name == "Family" }
        vm.enterSelectionWith(family)
        vm.cancelSelection()
        vm.state.value.selectionMode shouldBe false
        vm.state.value.selectedIds shouldBe emptySet()
    }

    @Test
    fun bulk_delete_removes_selected_categories_and_exits_selection_mode() {
        val vm = createViewModel()
        val family = vm.state.value.categories.first { it.name == "Family" }
        val weeknight = vm.state.value.categories.first { it.name == "Weeknight" }
        vm.enterSelectionWith(family)
        vm.toggleSelection(weeknight)
        vm.showBulkDeleteDialog()
        (vm.state.value.dialog as DialogState.BulkDelete).count shouldBe 2

        vm.confirmBulkDelete()

        vm.state.value.selectionMode shouldBe false
        vm.state.value.selectedIds shouldBe emptySet()
        vm.state.value.categories.map { it.name } shouldNotContain "Family"
        vm.state.value.categories.map { it.name } shouldNotContain "Weeknight"
    }

    @Test
    fun create_field_lifecycle_open_type_submit() {
        val vm = createViewModel()
        vm.openCreateField()
        vm.state.value.createState.shouldBeEditing()
        vm.updateCreateText("Brunch")
        (vm.state.value.createState as CreateState.Editing).text shouldBe "Brunch"
        vm.submitCreate()
        vm.state.value.createState shouldBe CreateState.Hidden
        vm.state.value.categories.map { it.name } shouldContain "Brunch"
    }

    @Test
    fun submit_create_with_blank_text_is_a_no_op() {
        val vm = createViewModel()
        vm.openCreateField()
        vm.updateCreateText("   ")
        vm.submitCreate()
        vm.state.value.createState.shouldBeEditing()
    }

    @Test
    fun rename_dialog_only_opens_for_editable_rows() {
        val vm = createViewModel()
        val builtin = vm.state.value.categories.first { it.builtinId == BuiltinCategory.OTHER.id }
        vm.showRenameDialog(builtin)
        vm.state.value.dialog shouldBe DialogState.None

        val family = vm.state.value.categories.first { it.name == "Family" }
        vm.showRenameDialog(family)
        (vm.state.value.dialog as DialogState.Rename).target.id shouldBe family.id
    }

    @Test
    fun rename_submit_applies_to_repository() {
        val vm = createViewModel()
        val family = vm.state.value.categories.first { it.name == "Family" }
        vm.submitRename(family.id, "Family Favorites")
        vm.state.value.categories.find { it.id == family.id }?.name shouldBe "Family Favorites"
        vm.state.value.dialog shouldBe DialogState.None
    }

    @Test
    fun delete_target_dialog_confirms_to_repository() {
        val vm = createViewModel()
        val family = vm.state.value.categories.first { it.name == "Family" }
        vm.showDeleteDialog(family)
        (vm.state.value.dialog as DialogState.Delete).target.id shouldBe family.id
        vm.confirmDelete()
        vm.state.value.categories.map { it.name } shouldNotContain "Family"
        vm.state.value.dialog shouldBe DialogState.None
    }

    private fun CreateState.shouldBeEditing(): CreateState.Editing {
        check(this is CreateState.Editing) { "Expected CreateState.Editing but was $this" }
        return this
    }
}
