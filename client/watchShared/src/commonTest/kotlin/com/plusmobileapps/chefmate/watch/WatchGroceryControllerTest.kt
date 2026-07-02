package com.plusmobileapps.chefmate.watch

import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.grocery.data.testing.FakeGroceryRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class WatchGroceryControllerTest {
    private class RecordingSessionImporter : WatchSessionImporter {
        var lastRefreshToken: String? = null

        override suspend fun importSession(refreshToken: String) {
            lastRefreshToken = refreshToken
        }
    }

    private fun TestScopeController(
        repo: FakeGroceryRepository = FakeGroceryRepository(),
        auth: FakeAuthenticationRepository = FakeAuthenticationRepository(),
        importer: RecordingSessionImporter = RecordingSessionImporter(),
        dispatcher: kotlin.coroutines.CoroutineContext,
    ) = WatchGroceryController(repo, auth, importer, dispatcher)

    @Test
    fun observeLists_emitsMappedDefaultList() = runTest {
        val controller = TestScopeController(dispatcher = UnconfinedTestDispatcher(testScheduler))
        val emissions = mutableListOf<List<WatchGroceryList>>()

        val cancellable = controller.observeLists { emissions.add(it) }
        advanceUntilIdle()

        assertEquals(1, emissions.last().size)
        assertEquals("My Grocery List", emissions.last().first().name)
        cancellable.cancel()
    }

    @Test
    fun addItem_thenSetChecked_reflectsInObservedItems() = runTest {
        val repo = FakeGroceryRepository()
        val controller =
            TestScopeController(repo = repo, dispatcher = UnconfinedTestDispatcher(testScheduler))
        val emissions = mutableListOf<List<WatchGroceryItem>>()
        val cancellable = controller.observeItems(listId = 1L) { emissions.add(it) }

        controller.addItem(listId = 1L, name = "2 apples")
        advanceUntilIdle()

        val added = emissions.last()
        assertEquals(1, added.size)
        assertFalse(added.first().isChecked)
        val itemId = added.first().id

        controller.setChecked(itemId, isChecked = true)
        advanceUntilIdle()

        assertTrue(emissions.last().first().isChecked)
        cancellable.cancel()
    }

    @Test
    fun observeSignedIn_tracksAuthState() = runTest {
        val auth = FakeAuthenticationRepository()
        val controller =
            TestScopeController(auth = auth, dispatcher = UnconfinedTestDispatcher(testScheduler))
        val emissions = mutableListOf<Boolean>()
        val cancellable = controller.observeSignedIn { emissions.add(it) }
        advanceUntilIdle()
        assertFalse(emissions.last())

        auth.setAuthenticated()
        advanceUntilIdle()
        assertTrue(emissions.last())
        cancellable.cancel()
    }

    @Test
    fun importSession_delegatesToImporter() = runTest {
        val importer = RecordingSessionImporter()
        val controller =
            TestScopeController(
                importer = importer,
                dispatcher = UnconfinedTestDispatcher(testScheduler),
            )

        controller.importSession("refresh-token-123")

        assertEquals("refresh-token-123", importer.lastRefreshToken)
    }
}
