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

    fun onDismissMessage()

    data class Model(
        val currentUrl: String = "",
        val navigateUrl: String = "",
        val addressBarText: String = "",
        val isExtracting: Boolean = false,
        val isWebViewLoading: Boolean = false,
        val extractionMessage: TextData? = null,
        val showControls: Boolean = true,
    )

    sealed class Output {
        data class RecipeExtracted(val recipeId: Long) : Output()
    }

    interface Factory {
        fun create(
            context: BlocContext,
            output: Consumer<Output>,
            showControls: Boolean = true,
        ): BrowserBloc
    }
}
