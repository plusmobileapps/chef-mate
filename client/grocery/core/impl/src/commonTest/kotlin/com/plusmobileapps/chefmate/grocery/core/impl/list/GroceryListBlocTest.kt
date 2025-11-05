@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.grocery.core.impl.list

import app.cash.turbine.test
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class GroceryListBlocTest {
    val context = TestBlocContext.Companion.create()
    val output = TestConsumer<GroceryListBloc.Output>()
    val groceries = MutableSharedFlow<List<GroceryItem>>()
    val repository: GroceryRepository =
        mock {
            every { getGroceries() } returns groceries.asSharedFlow()
        }

    val bloc =
        GroceryListBlocImpl(
            context = context,
            output = output,
            repository = repository,
        )

    @Test
    fun When_items_loaded_Then_state_is_updated() =
        runTest {
            bloc.state.test {
                awaitItem() shouldBe GroceryListBloc.Model(items = emptyList())
                val items =
                    listOf(
                        GroceryItem(1, "Apples", false),
                        GroceryItem(2, "Bananas", true),
                    )
                groceries.emit(items)
                awaitItem() shouldBe GroceryListBloc.Model(items = items)
            }
        }

    @Test
    fun When_grocery_item_checked_change_Then_repository_is_updated() =
        runTest {
            val item = GroceryItem(1, "Apples", false)
            everySuspend { repository.updateChecked(item, true) } returns Unit
            bloc.onGroceryItemCheckedChange(item, true)
            verifySuspend {
                repository.updateChecked(item, true)
            }
        }

    @Test
    fun When_grocery_item_deleted_Then_repository_is_updated() =
        runTest {
            val item = GroceryItem(1, "Apples", false)
            everySuspend { repository.deleteGrocery(item) } returns Unit
            bloc.onGroceryItemDelete(item)
            verifySuspend {
                repository.deleteGrocery(item)
            }
        }
}
