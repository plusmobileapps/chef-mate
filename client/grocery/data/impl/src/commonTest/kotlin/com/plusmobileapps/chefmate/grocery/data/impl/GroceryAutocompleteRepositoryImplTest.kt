@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.grocery.data.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.testing.createTestDatabase
import com.plusmobileapps.chefmate.grocery.data.impl.remote.GroceryAutocompleteRemoteDataSource
import com.plusmobileapps.chefmate.grocery.data.impl.remote.RemoteGroceryAutocompleteItem
import com.plusmobileapps.chefmate.util.testing.FakeUnique
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class GroceryAutocompleteRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val db: Database = createTestDatabase()
    private val fakeAuth = FakeAuthenticationRepository()
    private val fakeRemote = RecordingRemote()

    private val repository =
        GroceryAutocompleteRepositoryImpl(
            db = db.groceryAutocompleteItemQueries,
            ioContext = testDispatcher,
            unique = FakeUnique(),
            remoteDataSource = fakeRemote,
            authRepository = fakeAuth,
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

    @Test
    fun addItem_while_signed_out_does_not_push_to_remote() =
        runTest(testDispatcher) {
            repository.addItem("Tofu")

            fakeRemote.upserts shouldBe emptyList()
            db.groceryAutocompleteItemQueries.getByName("Tofu").executeAsOne().remoteId shouldBe
                null
        }

    @Test
    fun addItem_while_authenticated_pushes_and_stores_remoteId() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            repository.addItem("Tofu")

            fakeRemote.upserts.size shouldBe 1
            fakeRemote.upserts.first().name shouldBe "Tofu"
            db.groceryAutocompleteItemQueries.getByName("Tofu").executeAsOne().remoteId shouldBe
                "remote-uid-1"
        }

    @Test
    fun authenticating_pushes_previously_unsynced_items() =
        runTest(testDispatcher) {
            // Added while signed out — no remoteId.
            repository.addItem("Tofu")
            fakeRemote.upserts shouldBe emptyList()

            fakeAuth.setAuthenticated()

            fakeRemote.upserts.size shouldBe 1
            fakeRemote.upserts.first().name shouldBe "Tofu"
            db.groceryAutocompleteItemQueries.getByName("Tofu").executeAsOne().remoteId shouldBe
                "remote-uid-1"
        }

    @Test
    fun authenticating_pulls_remote_items_not_yet_local() =
        runTest(testDispatcher) {
            fakeRemote.remoteItems =
                listOf(
                    RemoteGroceryAutocompleteItem(
                        id = "remote-1",
                        ownerId = "test-id",
                        name = "Sriracha",
                        clientId = "client-1",
                    )
                )

            fakeAuth.setAuthenticated()

            repository.observeItems().test {
                val items = awaitItem()
                items.size shouldBe 1
                items.first().name shouldBe "Sriracha"
            }
            db.groceryAutocompleteItemQueries.getByName("Sriracha").executeAsOne().remoteId shouldBe
                "remote-1"
        }

    @Test
    fun pull_adopts_remoteId_onto_matching_local_name() =
        runTest(testDispatcher) {
            // A local row created offline shares a name (case-insensitively) with the incoming
            // remote row but has a different client_id: adopt the remoteId rather than duplicate.
            repository.addItem("Miso")
            fakeRemote.remoteItems =
                listOf(
                    RemoteGroceryAutocompleteItem(
                        id = "remote-9",
                        ownerId = "test-id",
                        name = "miso",
                        clientId = "some-other-device-client-id",
                    )
                )

            fakeAuth.setAuthenticated()

            repository.observeItems().test { awaitItem().size shouldBe 1 }
            db.groceryAutocompleteItemQueries.getByName("Miso").executeAsOne().remoteId shouldBe
                "remote-9"
        }

    @Test
    fun deleteItem_with_remoteId_deletes_on_remote() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            repository.addItem("Tofu")
            val id = repository.observeItems().first().first().id

            repository.deleteItem(id)

            fakeRemote.deletes shouldBe listOf("remote-uid-1")
        }

    /**
     * Records upserts/deletes and echoes back a synthesized remote id (`remote-<clientId>`), the
     * way Supabase would after an insert. [remoteItems] seeds what a pull returns.
     */
    private class RecordingRemote : GroceryAutocompleteRemoteDataSource {
        val upserts = mutableListOf<RemoteGroceryAutocompleteItem>()
        val deletes = mutableListOf<String>()
        var remoteItems: List<RemoteGroceryAutocompleteItem> = emptyList()

        override suspend fun upsertItem(
            item: RemoteGroceryAutocompleteItem
        ): RemoteGroceryAutocompleteItem =
            item.copy(id = item.id ?: "remote-${item.clientId}").also { upserts.add(it) }

        override suspend fun deleteItem(remoteId: String) {
            deletes.add(remoteId)
        }

        override suspend fun fetchAllItems(ownerId: String): List<RemoteGroceryAutocompleteItem> =
            remoteItems
    }
}
