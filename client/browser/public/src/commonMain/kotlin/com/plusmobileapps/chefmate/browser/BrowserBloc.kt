package com.plusmobileapps.chefmate.browser

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.text.TextData
import kotlinx.coroutines.flow.StateFlow

interface BrowserBloc {
    val state: StateFlow<Model>

    fun onUrlChanged(url: String)

    fun onNavigate()

    fun onUrlLoadedInWebView(url: String)

    fun onExtractRecipe()

    fun onDismissMessage()

    data class Model(
        val currentUrl: String = "",
        val addressBarText: String = "",
        val isExtracting: Boolean = false,
        val extractionMessage: TextData? = null,
    )

    sealed class Output {
        data class RecipeExtracted(val recipeId: Long) : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            output: Consumer<Output>,
        ): BrowserBloc
    }
}
