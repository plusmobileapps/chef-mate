package com.plusmobileapps.chefmate.recipe.core.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailScreen
import com.plusmobileapps.chefmate.recipe.core.edit.EditRecipeScreen

@Composable
fun RecipeRootScreen(
    recipeRootBloc: RecipeRootBloc,
    modifier: Modifier = Modifier,
) {
    Children(
        modifier = modifier.fillMaxSize(),
        stack = recipeRootBloc.routerState,
        animation = stackAnimation(slide()),
    ) { child ->
        when (val instance = child.instance) {
            is RecipeRootBloc.Child.Detail -> RecipeDetailScreen(bloc = instance.bloc)
            is RecipeRootBloc.Child.Edit -> EditRecipeScreen(bloc = instance.bloc)
        }
    }
}
