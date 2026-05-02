package com.plusmobileapps.chefmate.browser

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer

interface BrowserRootBloc {
    val routerState: Value<ChildStack<*, Child>>

    fun navigateToUrl(url: String)

    sealed class Child {
        data class Landing(val bloc: BrowserLandingBloc) : Child()

        data class EditQuery(val bloc: BrowserLandingBloc) : Child()

        data class Browser(val bloc: BrowserBloc) : Child()
    }

    sealed class Output {
        data class RecipeExtracted(val recipeId: Long) : Output()
    }

    interface Factory {
        fun create(
            context: BlocContext,
            output: Consumer<Output>,
            initialUrl: String? = null,
            showControls: Boolean = true,
        ): BrowserRootBloc
    }
}
