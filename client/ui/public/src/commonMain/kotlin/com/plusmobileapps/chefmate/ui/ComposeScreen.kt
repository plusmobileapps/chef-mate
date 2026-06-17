package com.plusmobileapps.chefmate.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Interface to provide a rendering of a screen in Compose. Can be used as the type in a Decompose
 * router so that no when statement is needed to then bind the rendering to the Bloc itself.
 */
interface ComposeScreen {
    @Composable fun Content(modifier: Modifier)
}

@Composable fun ComposeScreen.Content() = Content(Modifier)
