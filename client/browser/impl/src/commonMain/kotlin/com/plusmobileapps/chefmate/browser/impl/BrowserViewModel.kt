package com.plusmobileapps.chefmate.browser.impl

import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.browser.BrowserBloc
import com.plusmobileapps.chefmate.browser.BrowserHistoryRepository
import com.plusmobileapps.chefmate.di.Main
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Inject
class BrowserViewModel(
    @Main mainContext: CoroutineContext,
    private val recipeExtractorService: RecipeExtractorService,
    private val historyRepository: BrowserHistoryRepository,
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
        scope.launch { historyRepository.recordVisit(url) }
    }

    fun onWebViewLoadingChanged(isLoading: Boolean) {
        _state.value = _state.value.copy(isWebViewLoading = isLoading)
    }

    fun extractRecipe() {
        val url = _state.value.webViewReportedUrl.ifBlank { _state.value.currentUrl }
        if (url.isBlank() || _state.value.isExtracting) return

        _state.value = _state.value.copy(isExtracting = true, extractionFailed = false)

        scope.launch {
            try {
                val extracted = recipeExtractorService.extractRecipe(url)
                _state.value = _state.value.copy(isExtracting = false)
                output?.onNext(BrowserBloc.Output.RecipeExtracted(extracted))
            } catch (e: Exception) {
                Logger.d(e) { "Failed to extract recipe from $url" }
                _state.value = _state.value.copy(isExtracting = false, extractionFailed = true)
            }
        }
    }

    fun dismissMessage() {
        _state.value = _state.value.copy(extractionFailed = false)
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

    fun onAddressBarFocused() {
        val currentText = _state.value.addressBarText
        output?.onNext(BrowserBloc.Output.NavigateToLanding(currentText))
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
        val extractionFailed: Boolean = false,
        val canGoBack: Boolean = false,
        val canGoForward: Boolean = false,
        val goBackTrigger: Int = 0,
        val goForwardTrigger: Int = 0,
    )

    companion object {
        private const val DEFAULT_URL = "https://www.google.com"
    }
}
