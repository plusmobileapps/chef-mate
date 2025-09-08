package com.plusmobileapps.chefmate

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.plusmobileapps.chefmate.grocerylist.GroceryListScreen
import com.plusmobileapps.chefmate.root.RootBloc
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(rootBloc: RootBloc) {
    val state = rootBloc.state.subscribeAsState()
    MaterialTheme {
        Children(
            modifier = Modifier.fillMaxSize(),
            stack = state.value,
            animation = stackAnimation(slide()),
            content = {
                when (val child = it.instance) {
                    is RootBloc.Child.GroceryList -> GroceryListScreen(child.bloc)
                }
            }
        )
    }
}