@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.grocery.data.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.testing.createTestDatabase
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class GroceryAutocompleteRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val db: Database = createTestDatabase()

    private val repository =
        GroceryAutocompleteRepositoryImpl(
            db = db.groceryAutocompleteItemQueries,
            ioContext = testDispatcher,
        )

    @Test
    fun addItem_inserts_a_row_surfaced_by_observeItems() =
        runTest(testDispatcher) {
            repository.addItem("Kombucha")

            repository.observeItems().test {
                val items = awaitItem()
                items.size shouldBe 1
                items.first().name shouldBe "Kombucha"
            }
        }

    @Test
    fun addItem_trims_and_ignores_blanks() =
        runTest(testDispatcher) {
            repository.addItem("   ")
            repository.addItem("  Oat Milk  ")

            repository.observeItems().test {
                val items = awaitItem()
                items.size shouldBe 1
                items.first().name shouldBe "Oat Milk"
            }
        }

    @Test
    fun addItem_dedups_case_insensitively() =
        runTest(testDispatcher) {
            repository.addItem("Milk")
            repository.addItem("milk")

            repository.observeItems().test { awaitItem().size shouldBe 1 }
        }

    @Test
    fun deleteItem_removes_it_from_observed_list() =
        runTest(testDispatcher) {
            repository.addItem("Tofu")
            val id = repository.observeItems().first().first().id
            repository.deleteItem(id)

            repository.observeItems().test { awaitItem() shouldBe emptyList() }
        }

    @Test
    fun clearLocalData_removes_everything() =
        runTest(testDispatcher) {
            repository.addItem("Tofu")
            repository.addItem("Tempeh")
            repository.clearLocalData()

            repository.observeItems().test { awaitItem() shouldBe emptyList() }
        }
}
