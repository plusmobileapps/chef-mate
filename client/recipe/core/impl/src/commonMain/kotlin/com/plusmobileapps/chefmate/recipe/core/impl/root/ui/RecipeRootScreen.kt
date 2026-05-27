package com.plusmobileapps.chefmate.recipe.core.impl.root.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.chefmate.ui.backAnimation

@Composable
fun RecipeRootScreen(recipeRootBloc: RecipeRootBloc, modifier: Modifier = Modifier) {
    Children(
        modifier = modifier.fillMaxSize(),
        stack = recipeRootBloc.routerState,
        animation =
            backAnimation(
                backHandler = recipeRootBloc.backHandler,
                onBack = recipeRootBloc::onBackClicked,
            ),
    ) { child ->
        child.instance.Content(Modifier)
    }
}
