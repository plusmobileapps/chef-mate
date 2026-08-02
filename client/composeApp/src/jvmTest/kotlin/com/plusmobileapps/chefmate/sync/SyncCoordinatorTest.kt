@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.sync

import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.grocery.data.testing.FakeGroceryRepository
import com.plusmobileapps.chefmate.meal.data.testing.FakeMealPlanRepository
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.plusmobileapps.chefmate.recipebook.data.testing.FakeRecipeBookRepository
import com.plusmobileapps.chefmate.util.testing.FakeDateTimeUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class SyncCoordinatorTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeAuth = FakeAuthenticationRepository()
    private val dateTimeUtil = FakeDateTimeUtil()
    private val recipes = FakeRecipeRepository()
    private val books = FakeRecipeBookRepository()
    private val groceries = FakeGroceryRepository()
    private val meals = FakeMealPlanRepository()

    private val coordinator =
        SyncCoordinator(
            authRepository = fakeAuth,
            recipeBookRepository = books,
            recipeRepository = recipes,
            groceryRepository = groceries,
            mealPlanRepository = meals,
            dateTimeUtil = dateTimeUtil,
            ioContext = testDispatcher,
        )

    @Test
    fun syncAll_reconciles_every_repository_when_signed_in() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()

            assertEquals(SyncOutcome.Synced, coordinator.syncAll())

            assertEquals(1, books.syncAllUnsyncedCallCount)
            assertEquals(1, recipes.syncAllUnsyncedCallCount)
            assertEquals(1, groceries.syncAllUnsyncedCallCount)
            assertEquals(1, meals.syncAllUnsyncedCallCount)
        }

    @Test
    fun syncAll_does_nothing_when_signed_out() =
        runTest(testDispatcher) {
            assertEquals(SyncOutcome.SignedOut, coordinator.syncAll())

            assertEquals(0, recipes.syncAllUnsyncedCallCount)
        }

    @Test
    fun syncAll_refreshes_the_session_before_syncing() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()

            coordinator.syncAll()

            assertEquals(1, fakeAuth.refreshSessionCallCount)
        }

    @Test
    fun syncAll_skips_syncing_when_the_session_cannot_be_revived() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            fakeAuth.refreshSessionResult = false

            assertEquals(SyncOutcome.SessionExpired, coordinator.syncAll())

            // Every call would fail on the dead token anyway; reporting it beats failing silently.
            assertEquals(0, recipes.syncAllUnsyncedCallCount)
        }

    @Test
    fun syncAll_throttles_a_burst_of_triggers() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()

            assertEquals(SyncOutcome.Synced, coordinator.syncAll())
            assertEquals(SyncOutcome.Throttled, coordinator.syncAll())

            assertEquals(1, recipes.syncAllUnsyncedCallCount)
        }

    @Test
    fun syncAll_runs_again_once_the_throttle_window_passes() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            coordinator.syncAll()

            dateTimeUtil.fakeNow = dateTimeUtil.fakeNow + 2.minutes

            assertEquals(SyncOutcome.Synced, coordinator.syncAll())
            assertEquals(2, recipes.syncAllUnsyncedCallCount)
        }

    @Test
    fun syncAll_with_force_ignores_the_throttle() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            coordinator.syncAll()

            assertEquals(SyncOutcome.Synced, coordinator.syncAll(force = true))

            assertEquals(2, recipes.syncAllUnsyncedCallCount)
        }
}
