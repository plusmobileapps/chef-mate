@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.grocery.autocomplete.impl

import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteBloc.CreateState
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteBloc.DialogState
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteBloc.Item
import com.plusmobileapps.chefmate.grocery.data.GroceryAutocompleteItem
import com.plusmobileapps.chefmate.grocery.data.testing.FakeGroceryAutocompleteRepository
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class GroceryAutocompleteViewModelTest {

    private val items = MutableStateFlow<List<GroceryAutocompleteItem>>(emptyList())
    private val repository = FakeGroceryAutocompleteRepository(items)
    private val mainContext = UnconfinedTestDispatcher()

    private fun createViewModel() = GroceryAutocompleteViewModel(mainContext, repository)

    @Test
    fun loads_user_items_and_a_nonempty_defaults_list() {
        items.value = listOf(GroceryAutocompleteItem(id = 1L, name = "Kombucha"))
        val vm = createViewModel()

        vm.state.value.userItems shouldContain Item(id = 1L, name = "Kombucha")
        vm.state.value.defaults.isNotEmpty().shouldBeTrue()
        vm.state.value.isLoading shouldBe false
    }

    @Test
    fun defaults_exclude_names_the_user_has_added() {
        // "Milk" is a built-in default; adding it as a user item should drop it from defaults.
        items.value = listOf(GroceryAutocompleteItem(id = 1L, name = "Milk"))
        val vm = createViewModel()

        vm.state.value.defaults.map { it.lowercase() } shouldNotContain "milk"
    }

    @Test
    fun open_create_field_then_submit_adds_the_trimmed_item() {
        val vm = createViewModel()
        vm.openCreateField()
        vm.updateCreateText("  Sourdough starter  ")
        vm.submitCreate()

        vm.state.value.createState shouldBe CreateState.Hidden
        vm.state.value.userItems.map { it.name } shouldContain "Sourdough starter"
    }

    @Test
    fun submit_blank_is_ignored() {
        val vm = createViewModel()
        vm.openCreateField()
        vm.updateCreateText("   ")
        vm.submitCreate()

        vm.state.value.userItems shouldBe emptyList()
    }

    @Test
    fun delete_flow_removes_the_item() {
        items.value = listOf(GroceryAutocompleteItem(id = 7L, name = "Tofu"))
        val vm = createViewModel()
        val item = vm.state.value.userItems.first()

        vm.showDeleteDialog(item)
        vm.state.value.dialog shouldBe DialogState.Delete(item)
        vm.confirmDelete()

        vm.state.value.dialog shouldBe DialogState.None
        vm.state.value.userItems shouldBe emptyList()
    }
}
