package com.plusmobileapps.chefmate.browser

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.ui.BlocScreen

interface BrowserRootBloc : BlocScreen {
    val routerState: Value<ChildStack<*, Child>>

    fun navigateToUrl(url: String)

    sealed class Child {
        data class Landing(val bloc: BrowserLandingBloc) : Child(), BlocScreen by bloc

        data class EditQuery(val bloc: BrowserEditQueryBloc) : Child(), BlocScreen by bloc

        data class Browser(val bloc: BrowserBloc) : Child(), BlocScreen by bloc
    }

    sealed class Output {
        data class RecipeExtracted(val extracted: ExtractedRecipeData) : Output()
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
