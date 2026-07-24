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
    private val messageQueries = db.aiChatMessageQueries
    private val conversationQueries = db.aiChatConversationQueries
    private val dateTimeUtil = FakeDateTimeUtil(fakeNow = Instant.fromEpochMilliseconds(1_000_000L))
    private val geminiClient = mock<GeminiClient>()

    private val repository =
        AiChatRepository(
            messageQueries = messageQueries,
            conversationQueries = conversationQueries,
            geminiClient = geminiClient,
            dateTimeUtil = dateTimeUtil,
            ioContext = testDispatcher,
        )

    @Test
    fun sendMessage_creates_conversation_and_inserts_user_row_when_stream_is_empty() =
        runTest(testDispatcher) {
            // Verifies that a fresh conversation is created lazily with the user's first message,
            // and that no empty model placeholder leaks into the DB when the stream produces no
            // deltas.
            every { geminiClient.streamReply(any()) } returns emptyFlow()

            val conversationId =
                repository.sendMessage(conversationId = null, text = "hello gemini")

            val rows = messageQueries.getAllForConversation(conversationId!!).executeAsList()
            rows shouldHaveSize 1
            rows.first().role shouldBe "user"
            rows.first().content shouldBe "hello gemini"
            conversationQueries.observeAll().executeAsList() shouldHaveSize 1
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

            val conversationId = repository.sendMessage(conversationId = null, text = "hi")!!

            val rows = messageQueries.getAllForConversation(conversationId).executeAsList()
            rows.map { it.role } shouldBe listOf("user", "model")
            val model = rows.last()
            model.content shouldBe "Hello, world!"
            model.isStreaming shouldBe 0L
        }

    @Test
    fun sendMessage_appends_to_existing_conversation() =
        runTest(testDispatcher) {
            every { geminiClient.streamReply(any()) } returns flow { emit("hi") }
            val first = repository.sendMessage(conversationId = null, text = "hello")!!

            val second = repository.sendMessage(conversationId = first, text = "again")

            second shouldBe first
            messageQueries.getAllForConversation(first).executeAsList() shouldHaveSize 4
            conversationQueries.observeAll().executeAsList() shouldHaveSize 1
        }

    @Test
    fun sendMessage_uses_dateTimeUtil_for_createdAt() =
        runTest(testDispatcher) {
            dateTimeUtil.fakeNow = Instant.fromEpochMilliseconds(42L)
            every { geminiClient.streamReply(any()) } returns flow { emit("ok") }

            val id = repository.sendMessage(conversationId = null, text = "ping")!!

            val rows = messageQueries.getAllForConversation(id).executeAsList()
            rows.first().createdAt shouldBe 42L
            rows.last().createdAt shouldBe 42L
        }

    @Test
    fun sendMessage_rethrows_extractor_failure_and_persists_error_text() =
        runTest(testDispatcher) {
            every { geminiClient.streamReply(any()) } returns
                flow { throw GeminiException("MISSING_API_KEY") }

            assertFailsWith<GeminiException> {
                repository.sendMessage(conversationId = null, text = "hi")
            }

            val all = conversationQueries.observeAll().executeAsList()
            all shouldHaveSize 1
            val rows = messageQueries.getAllForConversation(all.first().id).executeAsList()
            rows shouldHaveSize 2
            rows.last().role shouldBe "model"
            rows.last().content shouldBe "MISSING_API_KEY"
            rows.last().isStreaming shouldBe 0L
        }

    @Test
    fun sendMessage_skips_blank_text_and_creates_no_conversation() =
        runTest(testDispatcher) {
            repository.sendMessage(conversationId = null, text = "   ") shouldBe null
            conversationQueries.observeAll().executeAsList() shouldBe emptyList()
        }

    @Test
    fun deleteConversation_cascades_to_messages() =
        runTest(testDispatcher) {
            every { geminiClient.streamReply(any()) } returns flow { emit("hi") }
            val id = repository.sendMessage(conversationId = null, text = "first")!!

            repository.deleteConversation(id)

            conversationQueries.observeAll().executeAsList() shouldBe emptyList()
            messageQueries.getAllForConversation(id).executeAsList() shouldBe emptyList()
        }

    @Test
    fun deleteAllConversations_removes_every_row() =
        runTest(testDispatcher) {
            every { geminiClient.streamReply(any()) } returns flow { emit("hi") }
            repository.sendMessage(conversationId = null, text = "one")
            repository.sendMessage(conversationId = null, text = "two")

            repository.deleteAllConversations()

            conversationQueries.observeAll().executeAsList() shouldBe emptyList()
        }

    @Test
    fun sendMessage_persists_recipeContextId_and_latest_lookup_finds_it() =
        runTest(testDispatcher) {
            every { geminiClient.streamReply(any()) } returns flow { emit("hi") }

            val id =
                repository.sendMessage(
                    conversationId = null,
                    text = "how long?",
                    recipeContextId = 7L,
                )!!

            repository.latestConversationIdForRecipe(7L) shouldBe id
            repository.recipeContextIdFor(id) shouldBe 7L
        }

    @Test
    fun latestConversationIdForRecipe_returns_most_recent_and_null_for_unknown() =
        runTest(testDispatcher) {
            every { geminiClient.streamReply(any()) } returns flow { emit("hi") }

            dateTimeUtil.fakeNow = Instant.fromEpochMilliseconds(1L)
            repository.sendMessage(conversationId = null, text = "older", recipeContextId = 7L)
            dateTimeUtil.fakeNow = Instant.fromEpochMilliseconds(2L)
            val newer =
                repository.sendMessage(
                    conversationId = null,
                    text = "newer",
                    recipeContextId = 7L,
                )!!

            // A different recipe's conversation must not be picked up.
            dateTimeUtil.fakeNow = Instant.fromEpochMilliseconds(3L)
            repository.sendMessage(conversationId = null, text = "other", recipeContextId = 8L)

            repository.latestConversationIdForRecipe(7L) shouldBe newer
            repository.latestConversationIdForRecipe(999L) shouldBe null
        }

    @Test
    fun recipeContextIdFor_is_null_when_conversation_had_no_recipe() =
        runTest(testDispatcher) {
            every { geminiClient.streamReply(any()) } returns flow { emit("hi") }
            val id = repository.sendMessage(conversationId = null, text = "standalone")!!

            repository.recipeContextIdFor(id) shouldBe null
        }
}
