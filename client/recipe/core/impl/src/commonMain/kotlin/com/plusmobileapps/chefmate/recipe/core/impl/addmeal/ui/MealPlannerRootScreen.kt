package com.plusmobileapps.chefmate.recipe.core.impl.addmeal.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.plusmobileapps.chefmate.recipe.core.addmeal.MealPlannerRootBloc
import com.plusmobileapps.chefmate.ui.backAnimation

@Composable
fun MealPlannerRootScreen(bloc: MealPlannerRootBloc, modifier: Modifier = Modifier) {
    val stackState = bloc.routerState.subscribeAsState()
    val hasBackStack = stackState.value.backStack.isNotEmpty()

    Children(
        modifier = modifier.fillMaxSize(),
        stack = bloc.routerState,
        animation = backAnimation(backHandler = bloc.backHandler, onBack = bloc::onBackClicked),
    ) { child ->
        when (val instance = child.instance) {
            is MealPlannerRootBloc.Child.RecipePicker ->
                RecipePickerScreen(bloc = instance.bloc, onCloseClick = bloc::onBackClicked)
            is MealPlannerRootBloc.Child.ChooseDate ->
                ChooseDateScreen(bloc = instance.bloc, showAsChild = hasBackStack)
            is MealPlannerRootBloc.Child.ChooseMealType ->
                ChooseMealTypeScreen(bloc = instance.bloc)
        }
    }
}
