package com.plusmobileapps.chefmate.browser

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.ui.ComposeScreen

interface BrowserRootBloc : ComposeScreen {
    val routerState: Value<ChildStack<*, Child>>

    @Composable
    override fun Content(modifier: Modifier) {
        BrowserRootScreen(bloc = this, modifier = modifier)
    }

    fun navigateToUrl(url: String)

    sealed class Child {

        abstract val bloc: ComposeScreen

        data class SelectEngine(override val bloc: BrowserSelectEngineBloc) : Child()

        data class Landing(override val bloc: BrowserLandingBloc) : Child()

        data class EditQuery(override val bloc: BrowserEditQueryBloc) : Child()

        data class Browser(override val bloc: BrowserBloc) : Child()
    }

    sealed class Output {
        data class RecipeExtracted(val extracted: ExtractedRecipeData) : Output()
    }

    sealed class Presentation {
        data object Embedded : Presentation()

        data class Modal(val onClose: () -> Unit) : Presentation()
    }

    interface Factory {
        fun create(
            context: BlocContext,
            output: Consumer<Output>,
            initialUrl: String? = null,
            showControls: Boolean = true,
            presentation: Presentation = Presentation.Embedded,
        ): BrowserRootBloc
    }
}
