@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.plusmobileapps.chefmate.browser.impl.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.plusmobileapps.chefmate.browser.BrowserRootBloc

@Composable
fun BrowserRootScreen(bloc: BrowserRootBloc, modifier: Modifier = Modifier) {
    val childStack by bloc.routerState.subscribeAsState()

    SharedTransitionLayout(modifier = modifier) {
        AnimatedContent(
            targetState = childStack.active.instance,
            transitionSpec = {
                fadeIn(tween(durationMillis = 400)) togetherWith
                    fadeOut(tween(durationMillis = 250))
            },
            label = "browser-root",
        ) { instance ->
            when (instance) {
                is BrowserRootBloc.Child.Landing ->
                    BrowserLandingScreen(
                        bloc = instance.bloc,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                        modifier = Modifier.fillMaxSize(),
                    )
                is BrowserRootBloc.Child.EditQuery ->
                    BrowserEditQueryScreen(
                        bloc = instance.bloc,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                        modifier = Modifier.fillMaxSize(),
                    )
                is BrowserRootBloc.Child.Browser ->
                    BrowserScreen(
                        bloc = instance.bloc,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                        modifier = Modifier.fillMaxSize(),
                    )
            }
        }
    }
}
