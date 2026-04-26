@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.browser.BrowserBloc
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class BrowserViewModelTest {

    private val extractorService: RecipeExtractorService = mock()
    private val recipeRepository: RecipeRepository = mock()
    private val outputs = mutableListOf<BrowserBloc.Output>()
    private val viewModel =
        BrowserViewModel(
                mainContext = UnconfinedTestDispatcher(),
                recipeExtractorService = extractorService,
                recipeRepository = recipeRepository,
            )
            .also { it.setOutput { output -> outputs.add(output) } }

    // region URL handling

    @Test
    fun initial_state_has_default_url() {
        val state = viewModel.state.value
        state.currentUrl shouldBe "https://www.google.com"
        state.addressBarText shouldBe "https://www.google.com"
    }

    @Test
    fun When_url_changed_Then_address_bar_text_updated() {
        viewModel.onUrlChanged("example.com")
        viewModel.state.value.addressBarText shouldBe "example.com"
    }

    @Test
    fun When_navigate_Then_https_prepended_and_current_url_updated() {
        viewModel.onUrlChanged("example.com")
        viewModel.onNavigate()
        val state = viewModel.state.value
        state.currentUrl shouldBe "https://example.com"
        state.addressBarText shouldBe "https://example.com"
    }

    @Test
    fun When_navigate_with_http_prefix_Then_not_double_prefixed() {
        viewModel.onUrlChanged("http://example.com")
        viewModel.onNavigate()
        viewModel.state.value.currentUrl shouldBe "http://example.com"
    }

    @Test
    fun When_navigate_with_https_prefix_Then_not_double_prefixed() {
        viewModel.onUrlChanged("https://example.com")
        viewModel.onNavigate()
        viewModel.state.value.currentUrl shouldBe "https://example.com"
    }

    @Test
    fun When_url_loaded_in_webview_Then_webViewReportedUrl_and_addressBarText_updated() {
        viewModel.onUrlLoadedInWebView("https://www.example.com/page")
        val state = viewModel.state.value
        state.webViewReportedUrl shouldBe "https://www.example.com/page"
        state.addressBarText shouldBe "https://www.example.com/page"
    }

    @Test
    fun When_url_loaded_in_webview_Then_currentUrl_not_changed() {
        viewModel.onUrlLoadedInWebView("https://www.example.com/page")
        viewModel.state.value.currentUrl shouldBe "https://www.google.com"
    }

    // endregion

    // region WebView loading state

    @Test
    fun When_webview_loading_changed_to_true_Then_state_reflects_loading() {
        viewModel.onWebViewLoadingChanged(true)
        viewModel.state.value.isWebViewLoading shouldBe true
    }

    @Test
    fun When_webview_loading_changed_to_false_Then_state_reflects_not_loading() {
        viewModel.onWebViewLoadingChanged(true)
        viewModel.onWebViewLoadingChanged(false)
        viewModel.state.value.isWebViewLoading shouldBe false
    }

    // endregion

    // region Recipe extraction

    @Test
    fun When_extract_recipe_succeeds_Then_shows_success_message_and_stores_recipe_id() = runTest {
        val extracted = testExtractedRecipe()
        val savedRecipe = testRecipe(id = 42)
        everySuspend { extractorService.extractRecipe(any()) } returns extracted
        everySuspend { recipeRepository.createRecipe(any()) } returns savedRecipe

        viewModel.onUrlChanged("https://example.com/recipe")
        viewModel.onNavigate()
        viewModel.extractRecipe()

        val state = viewModel.state.value
        state.isExtracting shouldBe false
        state.extractionMessage shouldBe BrowserViewModel.ExtractMessage.SUCCESS
        state.extractedRecipeId shouldBe 42
    }

    @Test
    fun When_extract_recipe_succeeds_Then_does_not_emit_output_immediately() = runTest {
        val extracted = testExtractedRecipe()
        val savedRecipe = testRecipe(id = 42)
        everySuspend { extractorService.extractRecipe(any()) } returns extracted
        everySuspend { recipeRepository.createRecipe(any()) } returns savedRecipe

        viewModel.onUrlChanged("https://example.com/recipe")
        viewModel.onNavigate()
        viewModel.extractRecipe()

        outputs shouldBe emptyList()
    }

    @Test
    fun When_extract_recipe_fails_Then_shows_failure_message() = runTest {
        everySuspend { extractorService.extractRecipe(any()) } throws
            IllegalStateException("No recipe")

        viewModel.onUrlChanged("https://example.com")
        viewModel.onNavigate()
        viewModel.extractRecipe()

        val state = viewModel.state.value
        state.isExtracting shouldBe false
        state.extractionMessage shouldBe BrowserViewModel.ExtractMessage.FAILURE
        state.extractedRecipeId shouldBe null
    }

    @Test
    fun When_extract_recipe_with_blank_url_Then_does_nothing() = runTest {
        viewModel.extractRecipe()
        viewModel.state.value.isExtracting shouldBe false
    }

    @Test
    fun When_already_extracting_Then_second_call_ignored() = runTest {
        val extracted = testExtractedRecipe()
        val savedRecipe = testRecipe(id = 1)
        everySuspend { extractorService.extractRecipe(any()) } returns extracted
        everySuspend { recipeRepository.createRecipe(any()) } returns savedRecipe

        viewModel.onUrlChanged("https://example.com/recipe")
        viewModel.onNavigate()
        viewModel.extractRecipe()

        // First call completes with UnconfinedTestDispatcher, verify only one call
        verifySuspend { extractorService.extractRecipe("https://example.com/recipe") }
    }

    @Test
    fun When_extract_uses_webViewReportedUrl_over_currentUrl() = runTest {
        val extracted = testExtractedRecipe()
        val savedRecipe = testRecipe(id = 1)
        everySuspend { extractorService.extractRecipe(any()) } returns extracted
        everySuspend { recipeRepository.createRecipe(any()) } returns savedRecipe

        viewModel.onUrlChanged("https://google.com")
        viewModel.onNavigate()
        viewModel.onUrlLoadedInWebView("https://example.com/actual-recipe")
        viewModel.extractRecipe()

        verifySuspend { extractorService.extractRecipe("https://example.com/actual-recipe") }
    }

    // endregion

    // region View extracted recipe

    @Test
    fun When_view_extracted_recipe_Then_emits_output_and_clears_state() = runTest {
        val extracted = testExtractedRecipe()
        val savedRecipe = testRecipe(id = 42)
        everySuspend { extractorService.extractRecipe(any()) } returns extracted
        everySuspend { recipeRepository.createRecipe(any()) } returns savedRecipe

        viewModel.onUrlChanged("https://example.com/recipe")
        viewModel.onNavigate()
        viewModel.extractRecipe()

        viewModel.onViewExtractedRecipe()

        outputs shouldBe listOf(BrowserBloc.Output.RecipeExtracted(42))
        val state = viewModel.state.value
        state.extractionMessage shouldBe null
        state.extractedRecipeId shouldBe null
    }

    @Test
    fun When_view_extracted_recipe_without_recipe_id_Then_does_nothing() {
        viewModel.onViewExtractedRecipe()
        outputs shouldBe emptyList()
    }

    // endregion

    // region Dismiss message

    @Test
    fun When_dismiss_message_Then_clears_message_and_recipe_id() = runTest {
        val extracted = testExtractedRecipe()
        val savedRecipe = testRecipe(id = 42)
        everySuspend { extractorService.extractRecipe(any()) } returns extracted
        everySuspend { recipeRepository.createRecipe(any()) } returns savedRecipe

        viewModel.onUrlChanged("https://example.com/recipe")
        viewModel.onNavigate()
        viewModel.extractRecipe()

        viewModel.dismissMessage()

        val state = viewModel.state.value
        state.extractionMessage shouldBe null
        state.extractedRecipeId shouldBe null
    }

    // endregion

    private fun testExtractedRecipe() =
        ExtractedRecipe(
            title = "Test Recipe",
            description = "A test recipe",
            ingredients = listOf("1 cup flour"),
            directions = listOf("Mix"),
            imageUrl = null,
            sourceUrl = "https://example.com/recipe",
            servings = 4,
            prepTime = 10,
            cookTime = 20,
            totalTime = 30,
            calories = 200,
        )

    private fun testRecipe(id: Long) =
        Recipe(
            id = id,
            title = "Test Recipe",
            description = "A test recipe",
            ingredients = "1 cup flour",
            directions = "Mix",
            imageUrl = null,
            sourceUrl = "https://example.com/recipe",
            servings = 4,
            prepTime = 10,
            cookTime = 20,
            totalTime = 30,
            calories = 200,
            starRating = null,
            isFavorite = false,
            createdAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST,
        )
}
