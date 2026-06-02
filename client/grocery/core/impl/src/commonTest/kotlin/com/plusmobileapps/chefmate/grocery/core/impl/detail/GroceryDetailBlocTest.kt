@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.grocery.core.impl.detail

import app.cash.turbine.test
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class GroceryDetailBlocTest {
    val groceryItem = GroceryItem(id = 1L, name = "Milk", isChecked = false)
    val repository: GroceryRepository = mock { everySuspend { getGrocery(1L) } returns groceryItem }
    val testConsumer = TestConsumer<GroceryDetailBloc.Output>()

    val bloc =
        GroceryDetailBlocImpl(
            context = TestBlocContext.create(),
            id = 1L,
            output = testConsumer,
            repository = repository,
        )

    @Test
    fun WHEN_grocery_loaded_THEN_update_state_with_grocery_details() {
        runTest {
            bloc.models.test {
                awaitItem() shouldBe GroceryDetailBloc.Model.Loaded(item = groceryItem)
            }
        }
    }

    @Test
    fun WHEN_grocery_name_changed_THEN_update_state_with_new_display_name() {
        runTest {
            bloc.onGroceryNameChanged("Bread")
            bloc.models.test {
                awaitItem() shouldBe
                    GroceryDetailBloc.Model.Loaded(item = groceryItem.copy(displayName = "Bread"))
            }
        }
    }

    @Test
    fun WHEN_grocery_quantity_changed_THEN_update_state_with_new_quantity() {
        runTest {
            bloc.onGroceryQuantityChanged("2 cups")
            bloc.models.test {
                awaitItem() shouldBe
                    GroceryDetailBloc.Model.Loaded(item = groceryItem.copy(quantity = "2 cups"))
            }
        }
    }

    @Test
    fun WHEN_quantity_cleared_THEN_quantity_becomes_null() {
        runTest {
            bloc.onGroceryQuantityChanged("2 cups")
            bloc.onGroceryQuantityChanged("")
            bloc.models.test {
                awaitItem() shouldBe
                    GroceryDetailBloc.Model.Loaded(item = groceryItem.copy(quantity = null))
            }
        }
    }

    @Test
    fun WHEN_grocery_checked_changed_THEN_update_state_with_new_isChecked() {
        runTest {
            bloc.onGroceryCheckedChanged(true)
            bloc.models.test {
                awaitItem() shouldBe
                    GroceryDetailBloc.Model.Loaded(item = groceryItem.copy(isChecked = true))
            }
        }
    }

    @Test
    fun WHEN_aisle_changed_THEN_update_state_with_new_category() {
        runTest {
            bloc.onAisleChanged(GroceryCategory.DAIRY)
            bloc.models.test {
                awaitItem() shouldBe
                    GroceryDetailBloc.Model.Loaded(
                        item = groceryItem.copy(category = GroceryCategory.DAIRY)
                    )
            }
        }
    }

    @Test
    fun WHEN_save_clicked_THEN_persist_combined_name_and_emit_finished_output() {
        runTest {
            everySuspend { repository.updateGrocery(any()) } returns Unit

            bloc.onGroceryQuantityChanged("2 cups")
            bloc.onGroceryNameChanged("Flour")
            bloc.onGroceryCheckedChanged(true)
            bloc.onSaveClicked()

            verifySuspend {
                repository.updateGrocery(
                    matches {
                        it.name == "2 cups Flour" &&
                            it.displayName == "Flour" &&
                            it.quantity == "2 cups" &&
                            it.isChecked
                    }
                )
            }
            testConsumer.lastValue shouldBe GroceryDetailBloc.Output.Finished
        }
    }

    @Test
    fun WHEN_save_clicked_with_no_quantity_THEN_persist_display_name_only() {
        runTest {
            everySuspend { repository.updateGrocery(any()) } returns Unit

            bloc.onGroceryNameChanged("Bread")
            bloc.onSaveClicked()

            verifySuspend {
                repository.updateGrocery(matches { it.name == "Bread" && it.quantity == null })
            }
            testConsumer.lastValue shouldBe GroceryDetailBloc.Output.Finished
        }
    }

    @Test
    fun GIVEN_blank_grocery_name_WHEN_save_clicked_THEN_do_not_update_repository_or_emit_finished_output() {
        runTest {
            bloc.onGroceryNameChanged("")
            bloc.onSaveClicked()

            verifySuspend { repository.getGrocery(1L) }
            testConsumer.values.size shouldBe 0
        }
    }

    @Test
    fun WHEN_back_clicked_THEN_emit_finished_output() {
        runTest {
            bloc.onBackClicked()
            testConsumer.lastValue shouldBe GroceryDetailBloc.Output.Finished
        }
    }
}
