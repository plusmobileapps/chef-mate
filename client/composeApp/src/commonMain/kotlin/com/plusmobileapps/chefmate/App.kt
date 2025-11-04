package com.plusmobileapps.chefmate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.root.RootBloc
import com.plusmobileapps.chefmate.root.RootScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    rootBloc: RootBloc,
    modifier: Modifier = Modifier,
) {
    RootScreen(rootBloc, modifier)
}
