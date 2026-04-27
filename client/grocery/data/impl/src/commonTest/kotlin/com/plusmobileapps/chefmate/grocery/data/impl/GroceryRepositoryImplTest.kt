@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)

package com.plusmobileapps.chefmate.grocery.data.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.auth.data.SignUpResult
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.testing.createTestDatabase
import com.plusmobileapps.chefmate.grocery.data.impl.remote.GroceryRemoteDataSource
import com.plusmobileapps.chefmate.grocery.data.impl.remote.RemoteGroceryItem
import com.plusmobileapps.chefmate.grocery.data.impl.remote.RemoteGroceryList
import com.plusmobileapps.chefmate.util.testing.FakeDateTimeUtil
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class GroceryRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val db: Database = createTestDatabase()
    private val fakeAuth = FakeAuthRepository()
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
}

private class FakeAuthRepository : AuthenticationRepository {
    private val _state = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val state: StateFlow<AuthState> = _state

    fun setState(state: AuthState) {
        _state.value = state
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String) =
        Result.success(Unit)

    override suspend fun signUpWithEmailAndPassword(email: String, password: String) =
        Result.success(SignUpResult.Success as SignUpResult)

    override suspend fun signOut() {
        _state.value = AuthState.Unauthenticated
    }

    override suspend fun sendPasswordResetEmail(email: String) = Result.success(Unit)
}

private class FakeGroceryRemoteDataSource : GroceryRemoteDataSource {
    val remoteLists = mutableMapOf<String, MutableList<RemoteGroceryList>>()
    val remoteItems = mutableMapOf<String, MutableList<RemoteGroceryItem>>()
    private var nextListId = 1

    override suspend fun ensureDefaultList(ownerId: String): String {
        val lists = remoteLists.getOrPut(ownerId) { mutableListOf() }
        if (lists.isNotEmpty()) return lists.first().id!!
        val newList =
            RemoteGroceryList(
                id = "remote-default-${nextListId++}",
                name = "My Grocery List",
                ownerId = ownerId,
            )
        lists.add(newList)
        return newList.id!!
    }

    override suspend fun upsertGroceryItem(item: RemoteGroceryItem): RemoteGroceryItem {
        val items = remoteItems.getOrPut(item.listId) { mutableListOf() }
        val existing = items.indexOfFirst { it.id == item.id || it.clientId == item.clientId }
        val result = item.copy(id = item.id ?: "remote-item-${Uuid.random()}")
        if (existing >= 0) {
            items[existing] = result
        } else {
            items.add(result)
        }
        return result
    }

    override suspend fun deleteGroceryItem(remoteId: String) {
        remoteItems.values.forEach { it.removeAll { item -> item.id == remoteId } }
    }

    override suspend fun fetchAllGroceryItems(listId: String): List<RemoteGroceryItem> =
        remoteItems[listId].orEmpty()

    override suspend fun createGroceryList(list: RemoteGroceryList): RemoteGroceryList {
        val result = list.copy(id = list.id ?: "remote-list-${nextListId++}")
        val ownerLists = remoteLists.getOrPut(list.ownerId) { mutableListOf() }
        ownerLists.add(result)
        return result
    }

    override suspend fun fetchGroceryLists(ownerId: String): List<RemoteGroceryList> =
        remoteLists[ownerId].orEmpty()

    override suspend fun deleteGroceryList(remoteId: String) {
        remoteLists.values.forEach { it.removeAll { list -> list.id == remoteId } }
    }

    override suspend fun updateGroceryList(list: RemoteGroceryList): RemoteGroceryList {
        val ownerLists = remoteLists[list.ownerId] ?: return list
        val index = ownerLists.indexOfFirst { it.id == list.id }
        if (index >= 0) ownerLists[index] = list
        return list
    }
}
