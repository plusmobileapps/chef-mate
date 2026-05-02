package com.plusmobileapps.chefmate.browser.impl

import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.browser.BrowserBloc
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Inject
class BrowserViewModel(
    @Main mainContext: CoroutineContext,
    private val recipeExtractorService: RecipeExtractorService,
    private val recipeRepository: RecipeRepository,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var output: Consumer<BrowserBloc.Output>? = null

    fun setOutput(output: Consumer<BrowserBloc.Output>) {
        this.output = output
    }

    fun onUrlChanged(url: String) {
        _state.value = _state.value.copy(addressBarText = url)
    }

    fun onNavigate() {
        val url = _state.value.addressBarText.ensureHttps()
        // Reset webViewReportedUrl so the BLoC's navigateUrl formula falls through to currentUrl,
        // ensuring a new navigation is always triggered even if the formula value didn't change.
        _state.value =
            _state.value.copy(currentUrl = url, addressBarText = url, webViewReportedUrl = "")
    }

    fun onUrlLoadedInWebView(url: String) {
        _state.value = _state.value.copy(webViewReportedUrl = url, addressBarText = url)
    }

    fun onWebViewLoadingChanged(isLoading: Boolean) {
        _state.value = _state.value.copy(isWebViewLoading = isLoading)
    }

    fun extractRecipe() {
        val url = _state.value.webViewReportedUrl.ifBlank { _state.value.currentUrl }
        if (url.isBlank() || _state.value.isExtracting) return

        _state.value = _state.value.copy(isExtracting = true, extractionMessage = null)

        scope.launch {
            try {
                val extracted = recipeExtractorService.extractRecipe(url)
                val recipe =
                    recipeRepository.createRecipe(
                        Recipe(
                            id = -1,
                            title = extracted.title,
                            description = extracted.description,
                            ingredients = extracted.ingredients.joinToString("\n"),
                            directions = extracted.directions.joinToString("\n"),
                            imageUrl = extracted.imageUrl,
                            sourceUrl = extracted.sourceUrl,
                            servings = extracted.servings,
                            prepTime = extracted.prepTime,
                            cookTime = extracted.cookTime,
                            totalTime = extracted.totalTime,
                            calories = extracted.calories,
                            starRating = null,
                            isFavorite = false,
                            createdAt = Instant.DISTANT_PAST,
                            updatedAt = Instant.DISTANT_PAST,
                        )
                    )
                _state.value =
                    _state.value.copy(
                        isExtracting = false,
                        extractionMessage = ExtractMessage.SUCCESS,
                        extractedRecipeId = recipe.id,
                    )
            } catch (e: Exception) {
                Logger.d(e) { "Failed to extract recipe from $url" }
                _state.value =
                    _state.value.copy(
                        isExtracting = false,
                        extractionMessage = ExtractMessage.FAILURE,
                    )
            }
        }
    }

    fun onViewExtractedRecipe() {
        val recipeId = _state.value.extractedRecipeId ?: return
        _state.value = _state.value.copy(extractionMessage = null, extractedRecipeId = null)
        output?.onNext(BrowserBloc.Output.RecipeExtracted(recipeId))
    }

    fun dismissMessage() {
        _state.value = _state.value.copy(extractionMessage = null, extractedRecipeId = null)
    }

    fun onCanNavigateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _state.value = _state.value.copy(canGoBack = canGoBack, canGoForward = canGoForward)
    }

    fun onGoBack() {
        _state.value = _state.value.copy(goBackTrigger = _state.value.goBackTrigger + 1)
    }

    fun onGoForward() {
        _state.value = _state.value.copy(goForwardTrigger = _state.value.goForwardTrigger + 1)
    }

    private fun String.ensureHttps(): String =
        if (!startsWith("http://") && !startsWith("https://")) {
            "https://$this"
        } else {
            this
        }

    data class State(
        val currentUrl: String = DEFAULT_URL,
        val addressBarText: String = DEFAULT_URL,
        val webViewReportedUrl: String = "",
        val isExtracting: Boolean = false,
        val isWebViewLoading: Boolean = false,
        val extractionMessage: ExtractMessage? = null,
        val extractedRecipeId: Long? = null,
        val canGoBack: Boolean = false,
        val canGoForward: Boolean = false,
        val goBackTrigger: Int = 0,
        val goForwardTrigger: Int = 0,
    )

    companion object {
        private const val DEFAULT_URL = "https://www.google.com"
    }

    enum class ExtractMessage {
        SUCCESS,
        FAILURE,
    }
}
