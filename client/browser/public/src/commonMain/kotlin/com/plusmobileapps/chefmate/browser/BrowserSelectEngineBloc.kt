package com.plusmobileapps.chefmate.browser

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen

/**
 * First-run screen where the user must explicitly choose a default search engine before the browser
 * landing screen becomes available. No default is preselected.
 */
interface BrowserSelectEngineBloc : ComposeScreen {

    @Composable
    override fun Content(modifier: Modifier) {
        BrowserSelectEngineScreen(bloc = this, modifier = modifier)
    }

    fun onEngineSelected(engine: SearchEngine)

    sealed class Output {
        data object EngineSelected : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): BrowserSelectEngineBloc
    }
}
