@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)

package com.plusmobileapps.chefmate.grocery.data.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.testing.createTestDatabase
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryItem
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryList
import com.plusmobileapps.chefmate.grocery.data.testing.FakeGroceryRemoteDataSource
import com.plusmobileapps.chefmate.util.testing.FakeDateTimeUtil
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class GroceryRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val db: Database = createTestDatabase()
    private val fakeAuth = FakeAuthenticationRepository()
    private val fakeRemote = FakeGroceryRemoteDataSource()
    private val dateTimeUtil = FakeDateTimeUtil()

    private val repository =
        GroceryRepositoryImpl(
            queries = db.groceryQueries,
            listQueries = db.groceryListQueries,
            ioContext = testDispatcher,
            dateTimeUtil = dateTimeUtil,
            remoteDataSource = fakeRemote,
            authRepository = fakeAuth,
        )

    @Test
    fun syncWithRemote_links_local_default_list_to_remote_instead_of_creating_duplicate() =
        runTest(testDispatcher) {
            // Simulate: user opened grocery screen on new device, creating a local default list
            val localListId = repository.ensureDefaultList()
            localListId shouldBe 1L

            // Verify only 1 list exists locally
            repository.getGroceryLists().test { awaitItem().size shouldBe 1 }

            // Remote has the user's existing "My Grocery List" from their old device
            val remoteListId = "remote-list-123"
            fakeRemote.remoteLists["user-1"] =
                mutableListOf(
                    RemoteGroceryList(
                        id = remoteListId,
                        name = "My Grocery List",
                        ownerId = "user-1",
                    )
                )

            // User signs in, triggering sync
            fakeAuth.setState(
                AuthState.Authenticated(
                    ChefMateUser(
                        userId = "user-1",
                        userName = "Test",
                        userEmail = "test@test.com",
                        userProfileImageUrl = null,
                    )
                )
            )

            // Should still have exactly 1 list — the local list linked to the remote one
            repository.getGroceryLists().test {
                val lists = awaitItem()
                lists.size shouldBe 1
                lists.first().name shouldBe "My Grocery List"
            }

            // Verify the local list was linked to the remote one (has remoteId)
            val localList = db.groceryListQueries.getByRemoteId(remoteListId).executeAsOneOrNull()
            localList shouldBe db.groceryListQueries.getById(localListId).executeAsOne()
        }

    @Test
    fun syncWithRemote_preserves_local_items_when_linking_to_remote_list() =
        runTest(testDispatcher) {
            // User adds items to local default list before signing in
            repository.addGrocery("Apples")
            repository.addGrocery("Bananas")

            val remoteListId = "remote-list-456"
            fakeRemote.remoteLists["user-1"] =
                mutableListOf(
                    RemoteGroceryList(
                        id = remoteListId,
                        name = "My Grocery List",
                        ownerId = "user-1",
                    )
                )

            // Sign in
            fakeAuth.setState(
                AuthState.Authenticated(
                    ChefMateUser(
                        userId = "user-1",
                        userName = "Test",
                        userEmail = "test@test.com",
                        userProfileImageUrl = null,
                    )
                )
            )

            // Still 1 list, and local items are preserved
            repository.getGroceryLists().test { awaitItem().size shouldBe 1 }
            repository.getGroceries().test {
                val items = awaitItem()
                items.size shouldBe 2
            }
        }

    @Test
    fun syncWithRemote_creates_new_local_list_for_unmatched_remote_list() =
        runTest(testDispatcher) {
            // Local default list exists
            repository.ensureDefaultList()

            // Remote has a list with a different name
            fakeRemote.remoteLists["user-1"] =
                mutableListOf(
                    RemoteGroceryList(
                        id = "remote-other",
                        name = "Party Supplies",
                        ownerId = "user-1",
                    )
                )

            fakeAuth.setState(
                AuthState.Authenticated(
                    ChefMateUser(
                        userId = "user-1",
                        userName = "Test",
                        userEmail = "test@test.com",
                        userProfileImageUrl = null,
                    )
                )
            )

            // Should have 2 lists: the local default + the remote "Party Supplies"
            repository.getGroceryLists().test {
                val lists = awaitItem()
                lists.size shouldBe 2
                lists.map { it.name }.toSet() shouldBe setOf("My Grocery List", "Party Supplies")
            }
        }

    @Test
    fun syncWithRemote_pulls_remote_items_into_linked_list() =
        runTest(testDispatcher) {
            repository.ensureDefaultList()

            val remoteListId = "remote-list-789"
            fakeRemote.remoteLists["user-1"] =
                mutableListOf(
                    RemoteGroceryList(
                        id = remoteListId,
                        name = "My Grocery List",
                        ownerId = "user-1",
                    )
                )
            fakeRemote.remoteItems[remoteListId] =
                mutableListOf(
                    RemoteGroceryItem(
                        id = "item-1",
                        listId = remoteListId,
                        name = "Milk",
                        clientId = Uuid.random().toString(),
                    ),
                    RemoteGroceryItem(
                        id = "item-2",
                        listId = remoteListId,
                        name = "Eggs",
                        clientId = Uuid.random().toString(),
                    ),
                )

            fakeAuth.setState(
                AuthState.Authenticated(
                    ChefMateUser(
                        userId = "user-1",
                        userName = "Test",
                        userEmail = "test@test.com",
                        userProfileImageUrl = null,
                    )
                )
            )

            // Remote items should be pulled into the linked list
            repository.getGroceries().test {
                val items = awaitItem()
                items.size shouldBe 2
                items.map { it.name }.toSet() shouldBe setOf("Milk", "Eggs")
            }
        }

    // ─── deleteAllGroceries ───────────────────────────────────────────────────

    @Test
    fun deleteAllGroceries_removes_all_items_from_local_db() =
        runTest(testDispatcher) {
            val listId = repository.ensureDefaultList()
            repository.addGrocery(listId, "Apples")
            repository.addGrocery(listId, "Bananas")

            repository.deleteAllGroceries(listId)

            repository.getGroceries(listId).test { awaitItem().isEmpty() shouldBe true }
        }

    @Test
    fun deleteAllGroceries_calls_remote_delete_for_each_synced_item() =
        runTest(testDispatcher) {
            val listId = repository.ensureDefaultList()
            val remoteListId = "remote-list-1"
            val now = "2026-01-01T00:00:00"
            db.groceryQueries.createWithRemoteId(
                name = "Apples",
                isChecked = false,
                createdAt = now,
                updatedAt = now,
                remoteId = "remote-apple",
                listRemoteId = remoteListId,
                clientId = "client-apple",
                listId = listId,
                recipeName = null,
            )
            db.groceryQueries.createWithRemoteId(
                name = "Bananas",
                isChecked = false,
                createdAt = now,
                updatedAt = now,
                remoteId = "remote-banana",
                listRemoteId = remoteListId,
                clientId = "client-banana",
                listId = listId,
                recipeName = null,
            )
            fakeRemote.remoteItems[remoteListId] =
                mutableListOf(
                    RemoteGroceryItem(id = "remote-apple", listId = remoteListId, name = "Apples"),
                    RemoteGroceryItem(id = "remote-banana", listId = remoteListId, name = "Bananas"),
                )

            repository.deleteAllGroceries(listId)

            fakeRemote.remoteItems[remoteListId].orEmpty().isEmpty() shouldBe true
        }

    @Test
    fun deleteAllGroceries_skips_remote_delete_when_items_have_no_remote_id() =
        runTest(testDispatcher) {
            val listId = repository.ensureDefaultList()
            repository.addGrocery(listId, "Apples") // no remoteId
            val unrelatedListId = "unrelated-list"
            fakeRemote.remoteItems[unrelatedListId] =
                mutableListOf(
                    RemoteGroceryItem(
                        id = "remote-item-x",
                        listId = unrelatedListId,
                        name = "Other",
                    )
                )

            repository.deleteAllGroceries(listId)

            fakeRemote.remoteItems[unrelatedListId]!!.size shouldBe 1
        }

    @Test
    fun deleteAllGroceries_only_deletes_items_from_the_specified_list() =
        runTest(testDispatcher) {
            val listId1 = repository.ensureDefaultList()
            val listId2 = repository.createGroceryList("Second List")
            repository.addGrocery(listId1, "Apples")
            repository.addGrocery(listId2, "Bananas")

            repository.deleteAllGroceries(listId1)

            repository.getGroceries(listId1).test { awaitItem().isEmpty() shouldBe true }
            repository.getGroceries(listId2).test { awaitItem().size shouldBe 1 }
        }

    // ─── deletePurchasedGroceries ─────────────────────────────────────────────

    @Test
    fun deletePurchasedGroceries_removes_only_checked_items_from_db() =
        runTest(testDispatcher) {
            val listId = repository.ensureDefaultList()
            repository.addGrocery(listId, "Apples")
            repository.addGrocery(listId, "Bananas")

            val items = db.groceryQueries.readByListId(listId).executeAsList()
            val toCheck = repository.getGrocery(items.first().id)!!
            repository.updateChecked(toCheck, isChecked = true)

            repository.deletePurchasedGroceries(listId)

            repository.getGroceries(listId).test {
                val remaining = awaitItem()
                remaining.size shouldBe 1
                remaining.first().isChecked shouldBe false
            }
        }

    @Test
    fun deletePurchasedGroceries_calls_remote_delete_only_for_checked_synced_items() =
        runTest(testDispatcher) {
            val listId = repository.ensureDefaultList()
            val remoteListId = "remote-list-checked"
            val now = "2026-01-01T00:00:00"
            db.groceryQueries.createWithRemoteId(
                name = "Milk",
                isChecked = true,
                createdAt = now,
                updatedAt = now,
                remoteId = "remote-milk",
                listRemoteId = remoteListId,
                clientId = "client-milk",
                listId = listId,
                recipeName = null,
            )
            db.groceryQueries.createWithRemoteId(
                name = "Eggs",
                isChecked = false,
                createdAt = now,
                updatedAt = now,
                remoteId = "remote-eggs",
                listRemoteId = remoteListId,
                clientId = "client-eggs",
                listId = listId,
                recipeName = null,
            )
            fakeRemote.remoteItems[remoteListId] =
                mutableListOf(
                    RemoteGroceryItem(
                        id = "remote-milk",
                        listId = remoteListId,
                        name = "Milk",
                        isChecked = true,
                    ),
                    RemoteGroceryItem(id = "remote-eggs", listId = remoteListId, name = "Eggs"),
                )

            repository.deletePurchasedGroceries(listId)

            val remaining = fakeRemote.remoteItems[remoteListId].orEmpty()
            remaining.size shouldBe 1
            remaining.first().name shouldBe "Eggs"
        }

    @Test
    fun deletePurchasedGroceries_is_a_no_op_when_no_items_are_checked() =
        runTest(testDispatcher) {
            val listId = repository.ensureDefaultList()
            repository.addGrocery(listId, "Apples")
            repository.addGrocery(listId, "Bananas")

            repository.deletePurchasedGroceries(listId)

            repository.getGroceries(listId).test { awaitItem().size shouldBe 2 }
        }
}
