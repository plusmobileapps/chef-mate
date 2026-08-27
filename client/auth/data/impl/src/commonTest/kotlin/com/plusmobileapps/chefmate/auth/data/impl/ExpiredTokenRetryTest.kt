@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.auth.data.impl

import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ExpiredTokenRetryTest {

    private val sentTokens = mutableListOf<String?>()
    private var refreshCalls = 0

    @Test
    fun replays_the_request_with_a_fresh_token_after_a_401() = runTest {
        val client = clientRespondingWith(HttpStatusCode.Unauthorized, HttpStatusCode.OK)

        val response =
            client.get("https://project.supabase.co/rest/v1/recipes") {
                bearerAuth("stale-token")
            }

        response.status shouldBe HttpStatusCode.OK
        refreshCalls shouldBe 1
        sentTokens shouldBe listOf("stale-token", "fresh-token")
    }

    @Test
    fun leaves_a_successful_request_alone() = runTest {
        val client = clientRespondingWith(HttpStatusCode.OK)

        client.get("https://project.supabase.co/rest/v1/recipes") { bearerAuth("good-token") }

        refreshCalls shouldBe 0
        sentTokens shouldBe listOf("good-token")
    }

    @Test
    fun does_not_retry_the_auth_endpoint() = runTest {
        // A dead refresh token makes /auth/v1/token answer 401. Refreshing in response to that
        // would call the same endpoint again — the loop this guard exists to prevent.
        val client = clientRespondingWith(HttpStatusCode.Unauthorized, HttpStatusCode.OK)

        val response =
            client.get("https://project.supabase.co/auth/v1/token") {
                bearerAuth("stale-token")
            }

        response.status shouldBe HttpStatusCode.Unauthorized
        refreshCalls shouldBe 0
        sentTokens shouldBe listOf("stale-token")
    }

    @Test
    fun lets_the_401_stand_when_the_session_cannot_be_renewed() = runTest {
        val client =
            clientRespondingWith(HttpStatusCode.Unauthorized, HttpStatusCode.OK) {
                refreshCalls += 1
                null
            }

        val response =
            client.get("https://project.supabase.co/rest/v1/recipes") {
                bearerAuth("stale-token")
            }

        // One doomed request beats two: the retry is skipped entirely.
        response.status shouldBe HttpStatusCode.Unauthorized
        refreshCalls shouldBe 1
        sentTokens shouldBe listOf("stale-token")
    }

    private fun clientRespondingWith(
        vararg statuses: HttpStatusCode,
        refresh: suspend (usedToken: String?) -> String? = {
            refreshCalls += 1
            "fresh-token"
        },
    ): HttpClient {
        var index = 0
        val engine = MockEngine { request ->
            sentTokens += request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
            respond(content = "", status = statuses[index++.coerceAtMost(statuses.lastIndex)])
        }
        return HttpClient(engine) {
            install(ExpiredTokenRetry) { refreshToken = refresh }
        }
    }
}
