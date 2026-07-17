package com.plusmobileapps.chefmate.aichat

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.backAnimation

@Composable
fun AiChatRootScreen(bloc: AiChatRootBloc, modifier: Modifier = Modifier) {
    // In the collapsed sheet "peek" the chat must hug its content height so the sheet stays small;
    // everywhere else it fills the available space.
    val sizeModifier =
        if (LocalAiChatPresentation.current == AiChatPresentation.SheetPeek) Modifier.fillMaxWidth()
        else Modifier.fillMaxSize()
    Children(
        modifier = modifier.then(sizeModifier),
        stack = bloc.routerState,
        animation = backAnimation(backHandler = bloc.backHandler, onBack = bloc::onBackClicked),
    ) { child ->
        child.instance.bloc.Content()
    }
}
