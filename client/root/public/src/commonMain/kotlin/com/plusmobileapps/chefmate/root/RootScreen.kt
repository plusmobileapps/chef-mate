package com.plusmobileapps.chefmate.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.plusmobileapps.chefmate.grocery.detail.GroceryDetailScreen
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavigationScreen
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootScreen

@Composable
fun RootScreen(rootBloc: RootBloc) {
    val state = rootBloc.state.subscribeAsState()
    MaterialTheme {
        Children(
            modifier = Modifier.fillMaxSize(),
            stack = state.value,
            animation = stackAnimation(slide()),
            content = {
                when (val child = it.instance) {
                    is RootBloc.Child.BottomNavigation -> BottomNavigationScreen(child.bloc)
                    is RootBloc.Child.GroceryDetail -> GroceryDetailScreen(child.bloc)
                    is RootBloc.Child.RecipeRoot -> RecipeRootScreen(child.bloc)
                }
            },
        )
    }
}
