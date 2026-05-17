package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.browser.BrowserBloc
import com.plusmobileapps.chefmate.browser.createExtractionFailedMessage
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = BrowserBloc.Factory::class)
class BrowserBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<BrowserBloc.Output>,
    @Assisted private val showControls: Boolean,
    viewModelFactory: Provider<BrowserViewModel>,
) : BrowserBloc, BlocContext by context {

    private val viewModel = instanceKeeper.getViewModel {
        viewModelFactory().also { it.setOutput(output) }
    }

    override val state: StateFlow<BrowserBloc.Model> =
        viewModel.state.mapState {
            BrowserBloc.Model(
                currentUrl = it.webViewReportedUrl.ifBlank { it.currentUrl },
                navigateUrl = it.webViewReportedUrl.ifBlank { it.currentUrl },
                addressBarText = it.addressBarText,
                isExtracting = it.isExtracting,
                isWebViewLoading = it.isWebViewLoading,
                showControls = showControls,
                extractionFailureMessage =
                    if (it.extractionFailed) createExtractionFailedMessage() else null,
                canGoBack = it.canGoBack,
                canGoForward = it.canGoForward,
                goBackTrigger = it.goBackTrigger,
                goForwardTrigger = it.goForwardTrigger,
            )
        }

    override fun onUrlChanged(url: String) {
        viewModel.onUrlChanged(url)
    }

    override fun onNavigate() {
        viewModel.onNavigate()
    }

    override fun onUrlLoadedInWebView(url: String) {
        viewModel.onUrlLoadedInWebView(url)
    }

    override fun onWebViewLoadingChanged(isLoading: Boolean) {
        viewModel.onWebViewLoadingChanged(isLoading)
    }

    override fun onExtractRecipe() {
        viewModel.extractRecipe()
    }

    override fun onDismissMessage() {
        viewModel.dismissMessage()
    }

    override fun onCanNavigateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        viewModel.onCanNavigateChanged(canGoBack, canGoForward)
    }

    override fun onGoBack() {
        viewModel.onGoBack()
    }

    override fun onGoForward() {
        viewModel.onGoForward()
    }

    override fun onAddressBarFocused() {
        viewModel.onAddressBarFocused()
    }
}
