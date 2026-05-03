@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.browser.BrowserBloc
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class BrowserViewModelTest {

    private val extractorService: RecipeExtractorService = mock()
    private val outputs = mutableListOf<BrowserBloc.Output>()
    private val viewModel =
        BrowserViewModel(
                mainContext = UnconfinedTestDispatcher(),
                recipeExtractorService = extractorService,
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
    fun When_extract_recipe_succeeds_Then_emits_output_with_extracted_data() = runTest {
        val extracted = testExtractedRecipe()
        everySuspend { extractorService.extractRecipe(any()) } returns extracted

        viewModel.onUrlChanged("https://example.com/recipe")
        viewModel.onNavigate()
        viewModel.extractRecipe()

        val state = viewModel.state.value
        state.isExtracting shouldBe false
        state.extractionFailed shouldBe false
        outputs shouldBe listOf(BrowserBloc.Output.RecipeExtracted(extracted))
    }

    @Test
    fun When_extract_recipe_fails_Then_marks_failure_and_emits_no_output() = runTest {
        everySuspend { extractorService.extractRecipe(any()) } throws
            IllegalStateException("No recipe")

        viewModel.onUrlChanged("https://example.com")
        viewModel.onNavigate()
        viewModel.extractRecipe()

        val state = viewModel.state.value
        state.isExtracting shouldBe false
        state.extractionFailed shouldBe true
        outputs shouldBe emptyList()
    }

    @Test
    fun When_extract_recipe_with_blank_url_Then_does_nothing() = runTest {
        viewModel.extractRecipe()
        viewModel.state.value.isExtracting shouldBe false
    }

    @Test
    fun When_already_extracting_Then_second_call_ignored() = runTest {
        val extracted = testExtractedRecipe()
        everySuspend { extractorService.extractRecipe(any()) } returns extracted

        viewModel.onUrlChanged("https://example.com/recipe")
        viewModel.onNavigate()
        viewModel.extractRecipe()

        // First call completes with UnconfinedTestDispatcher, verify only one call
        verifySuspend { extractorService.extractRecipe("https://example.com/recipe") }
    }

    @Test
    fun When_extract_uses_webViewReportedUrl_over_currentUrl() = runTest {
        val extracted = testExtractedRecipe()
        everySuspend { extractorService.extractRecipe(any()) } returns extracted

        viewModel.onUrlChanged("https://google.com")
        viewModel.onNavigate()
        viewModel.onUrlLoadedInWebView("https://example.com/actual-recipe")
        viewModel.extractRecipe()

        verifySuspend { extractorService.extractRecipe("https://example.com/actual-recipe") }
    }

    // endregion

    // region Dismiss message

    @Test
    fun When_dismiss_message_Then_clears_failure_state() = runTest {
        everySuspend { extractorService.extractRecipe(any()) } throws
            IllegalStateException("No recipe")

        viewModel.onUrlChanged("https://example.com/recipe")
        viewModel.onNavigate()
        viewModel.extractRecipe()

        viewModel.dismissMessage()

        viewModel.state.value.extractionFailed shouldBe false
    }

    // endregion

    private fun testExtractedRecipe() =
        ExtractedRecipeData(
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
}
