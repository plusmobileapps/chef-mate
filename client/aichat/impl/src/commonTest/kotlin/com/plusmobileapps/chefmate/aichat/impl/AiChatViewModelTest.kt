@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.plusmobileapps.chefmate.aichat.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.aichat.AiChatExtractionError
import com.plusmobileapps.chefmate.aichat.AiChatNoApiKeyError
import com.plusmobileapps.chefmate.aichat.ChatMessage
import com.plusmobileapps.chefmate.database.testing.createTestDatabase
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.util.testing.FakeDateTimeUtil
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class AiChatViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val db = createTestDatabase()
    private val dateTimeUtil = FakeDateTimeUtil()
    private val geminiClient = mock<GeminiClient>()
    private val recipeExtractor = mock<GeminiRecipeExtractor>()

    private val repository =
        AiChatRepository(
            queries = db.aiChatMessageQueries,
            geminiClient = geminiClient,
            dateTimeUtil = dateTimeUtil,
            ioContext = dispatcher,
        )

    private val viewModel =
        AiChatViewModel(
            mainContext = dispatcher,
            repository = repository,
            recipeExtractor = recipeExtractor,
        )

    @Test
    fun canAddRecipe_is_false_initially() =
        runTest(dispatcher) {
            viewModel.state.test {
                awaitItem().canAddRecipe shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun canAddRecipe_becomes_true_after_finished_model_reply() =
        runTest(dispatcher) {
            everySuspend { geminiClient.streamReply(any()) } returns flow { emit("answer") }

            viewModel.onInputChange("hi")
            viewModel.send()

            viewModel.state.test {
                val finalState = expectMostRecentItem()
                finalState.canAddRecipe shouldBe true
                finalState.messages.last().role shouldBe ChatMessage.Role.MODEL
                finalState.messages.last().isStreaming shouldBe false
            }
        }

    @Test
    fun extractRecipe_emits_extracted_recipe_then_clears_loading() =
        runTest(dispatcher) {
            everySuspend { geminiClient.streamReply(any()) } returns flow { emit("answer") }
            val extracted =
                ExtractedRecipeData(
                    title = "Test",
                    description = null,
                    ingredients = listOf("flour"),
                    directions = listOf("mix"),
                    imageUrl = null,
                    sourceUrl = "",
                    servings = null,
                    prepTime = null,
                    cookTime = null,
                    totalTime = null,
                    calories = null,
                )
            everySuspend { recipeExtractor.extract(any()) } returns extracted

            viewModel.onInputChange("hi")
            viewModel.send()

            viewModel.extractedRecipe.test {
                viewModel.extractRecipe()
                awaitItem() shouldBe extracted
                cancelAndIgnoreRemainingEvents()
            }
            viewModel.state.value.isExtractingRecipe shouldBe false
            viewModel.state.value.error shouldBe null
        }

    @Test
    fun extractRecipe_no_ops_when_no_model_reply_exists() =
        runTest(dispatcher) {
            viewModel.extractRecipe()

            viewModel.state.value.isExtractingRecipe shouldBe false
            viewModel.state.value.error shouldBe null
        }

    @Test
    fun extractRecipe_surfaces_missing_api_key_error() =
        runTest(dispatcher) {
            everySuspend { geminiClient.streamReply(any()) } returns flow { emit("ok") }
            everySuspend { recipeExtractor.extract(any()) } throws
                GeminiExtractionException("MISSING_API_KEY")

            viewModel.onInputChange("hi")
            viewModel.send()
            viewModel.extractRecipe()

            viewModel.state.value.error shouldBe AiChatNoApiKeyError
        }

    @Test
    fun extractRecipe_surfaces_generic_error_on_other_failures() =
        runTest(dispatcher) {
            everySuspend { geminiClient.streamReply(any()) } returns flow { emit("ok") }
            everySuspend { recipeExtractor.extract(any()) } throws
                GeminiExtractionException("MALFORMED_JSON")

            viewModel.onInputChange("hi")
            viewModel.send()
            viewModel.extractRecipe()

            viewModel.state.value.error shouldBe AiChatExtractionError
        }

    @Test
    fun send_clears_input_and_marks_isSending() =
        runTest(dispatcher) {
            everySuspend { geminiClient.streamReply(any()) } returns emptyFlow()
            viewModel.onInputChange("hello")

            viewModel.send()

            viewModel.inputText.value shouldBe ""
            viewModel.state.value.isSending shouldBe false
        }
}
