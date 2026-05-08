@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.recipe.bottomnav.impl

import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.DEFAULT_TAB_ORDER
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class BottomNavOrderViewModelTest {

    private val mainContext = UnconfinedTestDispatcher()

    @Test
    fun initial_state_mirrors_persisted_order_with_no_unsaved_changes() {
        val prefs = FakeTabOrderPreferences()
        val vm = BottomNavOrderViewModel(mainContext = mainContext, tabOrderPreferences = prefs)
        vm.state.value.editedOrder shouldBe DEFAULT_TAB_ORDER
        vm.state.value.persistedOrder shouldBe DEFAULT_TAB_ORDER
        vm.state.value.hasUnsavedChanges shouldBe false
    }

    @Test
    fun move_reorders_edited_order_and_marks_unsaved_changes() {
        val vm = newViewModel()
        // Move RECIPES (idx 0) to idx 2; edited order becomes GROCERIES, MEALS, RECIPES, BROWSER,
        // MORE
        vm.move(0, 2)
        vm.state.value.editedOrder shouldBe
            listOf(
                BottomNavBloc.Tab.GROCERIES,
                BottomNavBloc.Tab.MEALS,
                BottomNavBloc.Tab.RECIPES,
                BottomNavBloc.Tab.BROWSER,
                BottomNavBloc.Tab.SETTINGS,
            )
        vm.state.value.hasUnsavedChanges shouldBe true
    }

    @Test
    fun move_with_out_of_bounds_indices_is_a_no_op() {
        val vm = newViewModel()
        vm.move(from = -1, to = 3)
        vm.move(from = 0, to = 99)
        vm.move(from = 0, to = 0)
        vm.state.value.editedOrder shouldBe DEFAULT_TAB_ORDER
        vm.state.value.hasUnsavedChanges shouldBe false
    }

    @Test
    fun save_writes_edited_order_to_preferences_and_clears_unsaved_changes() {
        val prefs = FakeTabOrderPreferences()
        val vm = BottomNavOrderViewModel(mainContext = mainContext, tabOrderPreferences = prefs)
        vm.move(0, 4)
        vm.state.value.hasUnsavedChanges shouldBe true

        vm.save()

        val expected =
            listOf(
                BottomNavBloc.Tab.GROCERIES,
                BottomNavBloc.Tab.MEALS,
                BottomNavBloc.Tab.BROWSER,
                BottomNavBloc.Tab.SETTINGS,
                BottomNavBloc.Tab.RECIPES,
            )
        prefs.tabOrder.value shouldBe expected
        vm.state.value.persistedOrder shouldBe expected
        vm.state.value.hasUnsavedChanges shouldBe false
    }

    @Test
    fun cancel_path_dropping_view_model_does_not_mutate_persisted_order() {
        val prefs = FakeTabOrderPreferences()
        val vm = BottomNavOrderViewModel(mainContext = mainContext, tabOrderPreferences = prefs)
        vm.move(0, 3)

        // Simulating "cancel": discard the ViewModel without calling save(). The persisted order
        // must remain untouched, which is what the production path achieves by simply not calling
        // save() before the bloc's `Output.Back` is delivered.
        vm.onCleared()

        prefs.tabOrder.value shouldBe DEFAULT_TAB_ORDER
    }

    @Test
    fun external_preference_change_updates_persisted_order_in_state() {
        val prefs = FakeTabOrderPreferences()
        val vm = BottomNavOrderViewModel(mainContext = mainContext, tabOrderPreferences = prefs)

        val newOrder =
            listOf(
                BottomNavBloc.Tab.SETTINGS,
                BottomNavBloc.Tab.BROWSER,
                BottomNavBloc.Tab.MEALS,
                BottomNavBloc.Tab.GROCERIES,
                BottomNavBloc.Tab.RECIPES,
            )
        prefs.setTabOrder(newOrder)

        vm.state.value.persistedOrder shouldBe newOrder
    }

    private fun newViewModel(
        prefs: FakeTabOrderPreferences = FakeTabOrderPreferences()
    ): BottomNavOrderViewModel =
        BottomNavOrderViewModel(mainContext = mainContext, tabOrderPreferences = prefs)
}
