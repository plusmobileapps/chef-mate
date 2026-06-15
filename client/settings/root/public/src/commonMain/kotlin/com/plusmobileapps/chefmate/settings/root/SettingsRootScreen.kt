package com.plusmobileapps.chefmate.settings.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.backAnimation

@Composable
fun SettingsRootScreen(bloc: SettingsRootBloc, modifier: Modifier = Modifier) {
    Children(
        modifier = modifier.fillMaxSize().testTag(SettingsRootTestTags.SCREEN),
        stack = bloc.routerState,
        animation = backAnimation(backHandler = bloc.backHandler, onBack = bloc::onBackClicked),
    ) { child ->
        child.instance.bloc.Content()
    }
}
