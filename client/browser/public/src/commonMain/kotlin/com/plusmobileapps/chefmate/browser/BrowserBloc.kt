package com.plusmobileapps.chefmate.browser

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.text.TextData
import kotlinx.coroutines.flow.StateFlow

interface BrowserBloc {
    val state: StateFlow<Model>

    val instanceKeeper: InstanceKeeper

    fun onUrlChanged(url: String)

    fun onNavigate()

    fun onUrlLoadedInWebView(url: String)

    fun onWebViewLoadingChanged(isLoading: Boolean)

    fun onExtractRecipe()

    fun onViewExtractedRecipe()

    fun onDismissMessage()

    fun onCanNavigateChanged(canGoBack: Boolean, canGoForward: Boolean)

    fun onGoBack()

    fun onGoForward()

    fun onAddressBarFocused()

    data class Model(
        val currentUrl: String = "",
        val navigateUrl: String = "",
        val addressBarText: String = "",
        val isExtracting: Boolean = false,
        val isWebViewLoading: Boolean = false,
        val extractionMessage: TextData? = null,
        val hasExtractedRecipe: Boolean = false,
        val showControls: Boolean = true,
        val canGoBack: Boolean = false,
        val canGoForward: Boolean = false,
        val goBackTrigger: Int = 0,
        val goForwardTrigger: Int = 0,
    )

    sealed class Output {
        data class RecipeExtracted(val recipeId: Long) : Output()

        data class NavigateToLanding(val initialText: String) : Output()
    }

    interface Factory {
        fun create(
            context: BlocContext,
            output: Consumer<Output>,
            showControls: Boolean = true,
        ): BrowserBloc
    }
}
