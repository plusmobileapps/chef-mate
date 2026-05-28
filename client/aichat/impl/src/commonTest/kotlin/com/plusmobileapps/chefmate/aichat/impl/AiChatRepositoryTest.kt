@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.plusmobileapps.chefmate.aichat.impl

import com.plusmobileapps.chefmate.database.testing.createTestDatabase
import com.plusmobileapps.chefmate.util.testing.FakeDateTimeUtil
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class AiChatRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val db = createTestDatabase()
    private val queries = db.aiChatMessageQueries
    private val dateTimeUtil = FakeDateTimeUtil(fakeNow = Instant.fromEpochMilliseconds(1_000_000L))
    private val geminiClient = mock<GeminiClient>()

    private val repository =
        AiChatRepository(
            queries = queries,
            geminiClient = geminiClient,
            dateTimeUtil = dateTimeUtil,
            ioContext = testDispatcher,
        )

    @Test
    fun sendMessage_inserts_user_row_with_no_model_row_when_stream_is_empty() =
        runTest(testDispatcher) {
            // Verifies that the user row is persisted independently of any model reply, and that
            // no empty model placeholder leaks into the DB when the stream produces no deltas.
            every { geminiClient.streamReply(any()) } returns emptyFlow()

            repository.sendMessage("hello gemini")

            val rows = queries.getAll().executeAsList()
            rows shouldHaveSize 1
            rows.first().role shouldBe "user"
            rows.first().content shouldBe "hello gemini"
            verify { geminiClient.streamReply(any()) }
        }

    @Test
    fun sendMessage_accumulates_deltas_into_single_model_row() =
        runTest(testDispatcher) {
            every { geminiClient.streamReply(any()) } returns
                flow {
                    emit("Hello")
                    emit(", ")
                    emit("world!")
                }

            repository.sendMessage("hi")

            val rows = queries.getAll().executeAsList()
            rows.map { it.role } shouldBe listOf("user", "model")
            val model = rows.last()
            model.content shouldBe "Hello, world!"
            model.isStreaming shouldBe 0L
        }

    @Test
    fun sendMessage_uses_dateTimeUtil_for_createdAt() =
        runTest(testDispatcher) {
            dateTimeUtil.fakeNow = Instant.fromEpochMilliseconds(42L)
            every { geminiClient.streamReply(any()) } returns flow { emit("ok") }

            repository.sendMessage("ping")

            val rows = queries.getAll().executeAsList()
            rows.first().createdAt shouldBe 42L
            rows.last().createdAt shouldBe 42L
        }

    @Test
    fun sendMessage_rethrows_extractor_failure_and_persists_error_text() =
        runTest(testDispatcher) {
            every { geminiClient.streamReply(any()) } returns
                flow { throw GeminiException("MISSING_API_KEY") }

            assertFailsWith<GeminiException> { repository.sendMessage("hi") }

            val rows = queries.getAll().executeAsList()
            rows shouldHaveSize 2
            rows.last().role shouldBe "model"
            rows.last().content shouldBe "MISSING_API_KEY"
            rows.last().isStreaming shouldBe 0L
        }

    @Test
    fun sendMessage_skips_blank_text() =
        runTest(testDispatcher) {
            repository.sendMessage("   ")
            queries.getAll().executeAsList() shouldBe emptyList()
        }

    @Test
    fun clearHistory_removes_every_row() =
        runTest(testDispatcher) {
            every { geminiClient.streamReply(any()) } returns flow { emit("hi") }
            repository.sendMessage("first")

            repository.clearHistory()

            queries.getAll().executeAsList() shouldBe emptyList()
        }
}
