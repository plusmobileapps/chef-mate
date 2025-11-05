@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.grocery.core.impl.detail

import app.cash.turbine.test
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class GroceryDetailBlocTest {
    val groceryItem =
        GroceryItem(
            id = 1L,
            name = "Milk",
            isChecked = false,
        )
    val repository: GroceryRepository =
        mock {
            everySuspend { getGrocery(1L) } returns groceryItem
        }
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
                awaitItem() shouldBe
                    GroceryDetailBloc.Model.Loaded(
                        item = groceryItem,
                    )
            }
        }
    }

    @Test
    fun WHEN_grocery_name_changed_THEN_update_state_with_new_name() {
        runTest {
            bloc.onGroceryNameChanged("Bread")
            bloc.models.test {
                awaitItem() shouldBe
                    GroceryDetailBloc.Model.Loaded(
                        item = groceryItem.copy(name = "Bread"),
                    )
            }
        }
    }

    @Test
    fun WHEN_grocery_checked_changed_THEN_update_state_with_new_isChecked() {
        runTest {
            bloc.onGroceryCheckedChanged(true)
            bloc.models.test {
                awaitItem() shouldBe
                    GroceryDetailBloc.Model.Loaded(
                        item = groceryItem.copy(isChecked = true),
                    )
            }
        }
    }

    @Test
    fun WHEN_save_clicked_THEN_update_repository_and_emit_finished_output() {
        runTest {
            everySuspend { repository.updateGrocery(any()) } returns Unit

            bloc.onGroceryNameChanged("Bread")
            bloc.onGroceryCheckedChanged(true)
            bloc.onSaveClicked()

            bloc.models.test {
                awaitItem() shouldBe
                    GroceryDetailBloc.Model.Loaded(
                        GroceryItem(
                            id = 1L,
                            name = "Bread",
                            isChecked = true,
                        ),
                    )
                testConsumer.lastValue shouldBe GroceryDetailBloc.Output.Finished
            }
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
