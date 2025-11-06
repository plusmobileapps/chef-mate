package com.plusmobileapps.chefmate.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailScreen
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavigationScreen
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootScreen
import com.plusmobileapps.chefmate.ui.backAnimation
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun RootScreen(
    rootBloc: RootBloc,
    modifier: Modifier = Modifier,
) {
    val state = rootBloc.state.subscribeAsState()
    ChefMateTheme {
        Children(
            modifier = modifier.fillMaxSize(),
            stack = state.value,
            animation =
                backAnimation(
                    backHandler = rootBloc.backHandler,
                    onBack = rootBloc::onBackClicked,
                ),
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
