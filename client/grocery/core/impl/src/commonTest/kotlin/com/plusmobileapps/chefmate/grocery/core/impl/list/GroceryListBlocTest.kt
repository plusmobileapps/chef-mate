@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.grocery.core.impl.list

import app.cash.turbine.test
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc.GroceryGroup
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import com.russhwolf.settings.MapSettings
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.runTest

class GroceryListBlocTest {
    val context = TestBlocContext.Companion.create()
    val output = TestConsumer<GroceryListBloc.Output>()
    val groceries = MutableSharedFlow<List<GroceryItem>>()
    val groceryLists = MutableSharedFlow<List<GroceryListModel>>()
    val settings = MapSettings()
    val repository: GroceryRepository = mock {
        every { getGroceries() } returns groceries.asSharedFlow()
        every { getGroceries(1L) } returns groceries.asSharedFlow()
        every { getGroceryLists() } returns groceryLists.asSharedFlow()
        everySuspend { ensureDefaultList() } returns 1L
    }

    val bloc =
        GroceryListBlocImpl(
            context = context,
            output = output,
            viewModelFactory = {
                GroceryListViewModel(
                    mainContext = context.mainContext,
                    repository = repository,
                    settings = settings,
                )
            },
        )

    @Test
    fun When_items_loaded_Then_state_is_updated() = runTest {
        bloc.state.test {
            awaitItem() shouldBe GroceryListBloc.Model()
            val items =
                listOf(
                    GroceryItem(
                        id = 1,
                        name = "Apples",
                        category = GroceryCategory.PRODUCE,
                        isChecked = false,
                    ),
                    GroceryItem(
                        id = 2,
                        name = "Bananas",
                        category = GroceryCategory.PRODUCE,
                        isChecked = true,
                    ),
                )
            groceries.emit(items)
            awaitItem() shouldBe
                GroceryListBloc.Model(
                    groupedItems =
                        listOf(GroceryGroup(category = GroceryCategory.PRODUCE, items = items))
                )
        }
    }

    @Test
    fun When_grocery_item_checked_change_Then_repository_is_updated() = runTest {
        val item = GroceryItem(id = 1, name = "Apples", isChecked = false)
        everySuspend { repository.updateChecked(item, true) } returns Unit
        bloc.onGroceryItemCheckedChange(item, true)
        verifySuspend { repository.updateChecked(item, true) }
    }

    @Test
    fun When_grocery_item_deleted_Then_repository_is_updated() = runTest {
        val item = GroceryItem(id = 1, name = "Apples", isChecked = false)
        everySuspend { repository.deleteGrocery(item) } returns Unit
        bloc.onGroceryItemDelete(item)
        verifySuspend { repository.deleteGrocery(item) }
    }

    @Test
    fun When_alphabetical_sort_applied_Then_items_sorted_alphabetically() = runTest {
        val items =
            listOf(
                GroceryItem(
                    id = 1,
                    name = "Chicken",
                    displayName = "Chicken",
                    category = GroceryCategory.MEAT,
                    isChecked = false,
                ),
                GroceryItem(
                    id = 2,
                    name = "Apples",
                    displayName = "Apples",
                    category = GroceryCategory.PRODUCE,
                    isChecked = false,
                ),
                GroceryItem(
                    id = 3,
                    name = "Bread",
                    displayName = "Bread",
                    category = GroceryCategory.BAKERY,
                    isChecked = false,
                ),
            )
        groceries.emit(items)
        bloc.onApplySortAndFilter(
            GroceryListBloc.GrocerySort.ALPHABETICAL,
            GroceryListBloc.GroceryFilter.ALL,
        )
        bloc.state.test {
            val result = awaitItem()
            result.sort shouldBe GroceryListBloc.GrocerySort.ALPHABETICAL
            result.groupedItems.size shouldBe 1
            result.groupedItems[0].items.map { it.displayName } shouldBe
                listOf("Apples", "Bread", "Chicken")
        }
    }

    @Test
    fun When_sort_and_filter_applied_Then_state_reflects_both() = runTest {
        val items =
            listOf(
                GroceryItem(
                    id = 1,
                    name = "Apples",
                    displayName = "Apples",
                    category = GroceryCategory.PRODUCE,
                    isChecked = true,
                ),
                GroceryItem(
                    id = 2,
                    name = "Bananas",
                    displayName = "Bananas",
                    category = GroceryCategory.PRODUCE,
                    isChecked = false,
                ),
            )
        groceries.emit(items)
        bloc.onApplySortAndFilter(
            GroceryListBloc.GrocerySort.ALPHABETICAL,
            GroceryListBloc.GroceryFilter.UNPURCHASED,
        )
        bloc.state.test {
            val result = awaitItem()
            result.sort shouldBe GroceryListBloc.GrocerySort.ALPHABETICAL
            result.filter shouldBe GroceryListBloc.GroceryFilter.UNPURCHASED
            result.groupedItems.flatMap { it.items }.size shouldBe 1
            result.groupedItems.flatMap { it.items }.first().displayName shouldBe "Bananas"
        }
    }

    @Test
    fun When_items_have_recipe_name_Then_model_contains_recipe_name() = runTest {
        bloc.state.test {
            awaitItem() shouldBe GroceryListBloc.Model()
            val items =
                listOf(
                    GroceryItem(
                        id = 1,
                        name = "Flour",
                        displayName = "Flour",
                        category = GroceryCategory.BAKING,
                        isChecked = false,
                        recipeName = "Chocolate Cake",
                    ),
                    GroceryItem(
                        id = 2,
                        name = "Sugar",
                        displayName = "Sugar",
                        category = GroceryCategory.BAKING,
                        isChecked = false,
                    ),
                )
            groceries.emit(items)
            val result = awaitItem()
            val bakingItems = result.groupedItems.first { it.category == GroceryCategory.BAKING }
            bakingItems.items[0].recipeName shouldBe "Chocolate Cake"
            bakingItems.items[1].recipeName shouldBe null
        }
    }
}
