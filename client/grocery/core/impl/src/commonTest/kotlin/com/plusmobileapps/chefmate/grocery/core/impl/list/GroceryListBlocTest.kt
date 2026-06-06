@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.grocery.core.impl.list

import app.cash.turbine.test
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc.GroceryGroup
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import com.russhwolf.settings.MapSettings
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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

    var detailItemId: Long? = null
    var detailOutput: Consumer<GroceryDetailBloc.Output> = Consumer {}
    val groceryDetailFactory = GroceryDetailBloc.Factory { _, id, output ->
        detailItemId = id
        detailOutput = output
        mock(MockMode.autoUnit)
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
            groceryDetailFactory = groceryDetailFactory,
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
                        persistentListOf(
                            GroceryGroup(
                                category = GroceryCategory.PRODUCE,
                                items = items.toImmutableList(),
                            )
                        ),
                    hasNoRecipeItems = true,
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
    fun When_recipe_filter_applied_Then_only_items_from_that_recipe_shown() = runTest {
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
                    recipeName = "Chocolate Cake",
                ),
                GroceryItem(
                    id = 3,
                    name = "Chicken",
                    displayName = "Chicken",
                    category = GroceryCategory.MEAT,
                    isChecked = false,
                    recipeName = "Chicken Soup",
                ),
                GroceryItem(
                    id = 4,
                    name = "Salt",
                    displayName = "Salt",
                    category = GroceryCategory.SPICES,
                    isChecked = false,
                ),
            )
        groceries.emit(items)
        bloc.onApplySortAndFilter(
            GroceryListBloc.GrocerySort.AISLE,
            GroceryListBloc.GroceryFilter.ALL,
            recipeFilter = "Chocolate Cake",
        )
        bloc.state.test {
            val result = awaitItem()
            result.recipeFilter shouldBe "Chocolate Cake"
            val allItems = result.groupedItems.flatMap { it.items }
            allItems.size shouldBe 2
            allItems.map { it.displayName } shouldBe listOf("Flour", "Sugar")
        }
    }

    @Test
    fun When_no_recipe_filter_applied_Then_only_items_without_recipe_shown() = runTest {
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
                    name = "Salt",
                    displayName = "Salt",
                    category = GroceryCategory.SPICES,
                    isChecked = false,
                ),
            )
        groceries.emit(items)
        bloc.onApplySortAndFilter(
            GroceryListBloc.GrocerySort.AISLE,
            GroceryListBloc.GroceryFilter.ALL,
            recipeFilter = GroceryListBloc.NO_RECIPE_FILTER,
        )
        bloc.state.test {
            val result = awaitItem()
            result.recipeFilter shouldBe GroceryListBloc.NO_RECIPE_FILTER
            val allItems = result.groupedItems.flatMap { it.items }
            allItems.size shouldBe 1
            allItems.first().displayName shouldBe "Salt"
        }
    }

    @Test
    fun When_available_recipes_exist_Then_model_contains_sorted_recipe_names() = runTest {
        bloc.state.test {
            awaitItem() shouldBe GroceryListBloc.Model()
            val items =
                listOf(
                    GroceryItem(
                        id = 1,
                        name = "Flour",
                        category = GroceryCategory.BAKING,
                        isChecked = false,
                        recipeName = "Chocolate Cake",
                    ),
                    GroceryItem(
                        id = 2,
                        name = "Chicken",
                        category = GroceryCategory.MEAT,
                        isChecked = false,
                        recipeName = "Chicken Soup",
                    ),
                    GroceryItem(
                        id = 3,
                        name = "Salt",
                        category = GroceryCategory.SPICES,
                        isChecked = false,
                    ),
                )
            groceries.emit(items)
            val result = awaitItem()
            result.availableRecipes shouldBe listOf("Chicken Soup", "Chocolate Cake")
            result.hasNoRecipeItems shouldBe true
        }
    }

    @Test
    fun When_clear_filters_applied_Then_recipe_filter_is_also_cleared() = runTest {
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
                    name = "Salt",
                    displayName = "Salt",
                    category = GroceryCategory.SPICES,
                    isChecked = false,
                ),
            )
        groceries.emit(items)
        bloc.onApplySortAndFilter(
            GroceryListBloc.GrocerySort.ALPHABETICAL,
            GroceryListBloc.GroceryFilter.ALL,
            recipeFilter = "Chocolate Cake",
        )
        bloc.onApplySortAndFilter(
            GroceryListBloc.GrocerySort.AISLE,
            GroceryListBloc.GroceryFilter.ALL,
            recipeFilter = null,
        )
        bloc.state.test {
            val result = awaitItem()
            result.recipeFilter shouldBe null
            result.sort shouldBe GroceryListBloc.GrocerySort.AISLE
            result.filter shouldBe GroceryListBloc.GroceryFilter.ALL
            result.groupedItems.flatMap { it.items }.size shouldBe 2
        }
    }

    @Test
    fun When_clear_filters_clicked_Then_filters_reset_but_sort_preserved() = runTest {
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
                    name = "Salt",
                    displayName = "Salt",
                    category = GroceryCategory.SPICES,
                    isChecked = true,
                ),
            )
        groceries.emit(items)
        // Apply a sort + a purchase filter + a recipe filter that together hide everything.
        bloc.onApplySortAndFilter(
            GroceryListBloc.GrocerySort.ALPHABETICAL,
            GroceryListBloc.GroceryFilter.UNPURCHASED,
            recipeFilter = "Chocolate Cake",
        )
        bloc.onClearFiltersClicked()
        bloc.state.test {
            val result = awaitItem()
            result.filter shouldBe GroceryListBloc.GroceryFilter.ALL
            result.recipeFilter shouldBe null
            // Sort is intentionally preserved — it never causes an empty filtered list.
            result.sort shouldBe GroceryListBloc.GrocerySort.ALPHABETICAL
            result.groupedItems.flatMap { it.items }.size shouldBe 2
        }
    }

    @Test
    fun When_browse_recipes_clicked_Then_open_recipes_output_emitted() = runTest {
        bloc.onBrowseRecipesClicked()
        output.lastValue shouldBe GroceryListBloc.Output.OpenRecipes
    }

    @Test
    fun When_item_clicked_Then_detail_sheet_slot_activated() = runTest {
        val item = GroceryItem(id = 42, name = "Apples", isChecked = false)
        bloc.childSlot.value.child shouldBe null
        bloc.onGroceryItemClicked(item)
        bloc.childSlot.value.child?.instance.let {
            (it as GroceryListBloc.Sheet.GroceryDetail).bloc shouldBe it.bloc
        }
        detailItemId shouldBe 42L
    }

    @Test
    fun When_detail_finishes_Then_sheet_slot_dismissed() = runTest {
        val item = GroceryItem(id = 7, name = "Milk", isChecked = false)
        bloc.onGroceryItemClicked(item)
        bloc.childSlot.value.child shouldNotBe null
        detailOutput.onNext(GroceryDetailBloc.Output.Finished)
        bloc.childSlot.value.child shouldBe null
    }

    @Test
    fun When_dismiss_sheet_called_Then_slot_dismissed() = runTest {
        val item = GroceryItem(id = 9, name = "Cheese", isChecked = false)
        bloc.onGroceryItemClicked(item)
        bloc.childSlot.value.child shouldNotBe null
        bloc.onDismissSheet()
        bloc.childSlot.value.child shouldBe null
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
