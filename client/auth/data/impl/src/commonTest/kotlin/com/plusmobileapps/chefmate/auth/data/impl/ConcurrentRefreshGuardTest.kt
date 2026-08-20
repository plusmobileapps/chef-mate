@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.auth.data.impl

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class ConcurrentRefreshGuardTest {

    private var token: String? = "stale"
    private var refreshCalls = 0

    private val guard =
        ConcurrentRefreshGuard(
            currentToken = { token },
            refresh = {
                refreshCalls += 1
                // A real refresh is a network round trip. Suspending here is what makes the
                // burst test meaningful: without the lock, every waiting caller would sail past
                // the "has someone already refreshed?" check while this one is still in flight.
                delay(10)
                token = "fresh-$refreshCalls"
            },
        )

    @Test
    fun refreshes_when_the_caller_used_the_current_token() = runTest {
        guard.tokenAfterRefresh(usedToken = "stale") shouldBe "fresh-1"

        refreshCalls shouldBe 1
    }

    @Test
    fun reuses_a_refresh_another_caller_already_did() = runTest {
        token = "already-fresh"

        guard.tokenAfterRefresh(usedToken = "stale") shouldBe "already-fresh"

        refreshCalls shouldBe 0
    }

    @Test
    fun a_burst_of_401s_on_the_same_token_causes_one_refresh() = runTest {
        // Supabase rotates the refresh token on every use, so parallel refreshes read as replay
        // and can invalidate the session — the sign-in this code exists to rescue.
        val results = List(8) { async { guard.tokenAfterRefresh(usedToken = "stale") } }.awaitAll()

        refreshCalls shouldBe 1
        results.distinct() shouldBe listOf("fresh-1")
    }

    @Test
    fun returns_null_when_the_refresh_fails() = runTest {
        val failing =
            ConcurrentRefreshGuard(
                currentToken = { token },
                refresh = { throw RuntimeException("offline") },
            )

        // Not the stale token: handing that back would only buy a second 401.
        failing.tokenAfterRefresh(usedToken = "stale") shouldBe null
    }
}
