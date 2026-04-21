package com.plusmobileapps.chefmate.meal.core.addmealsheet

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.plusmobileapps.chefmate.meal.core.recipepicker.RecipePickerScreen
import com.plusmobileapps.chefmate.recipe.core.addmeal.AddToMealPlanScreen
import com.plusmobileapps.chefmate.ui.backAnimation

@Composable
fun AddMealSheetScreen(bloc: AddMealSheetBloc, modifier: Modifier = Modifier) {
    Children(
        modifier = modifier.fillMaxSize(),
        stack = bloc.routerState,
        animation = backAnimation(backHandler = bloc.backHandler, onBack = bloc::onBackClicked),
    ) { child ->
        when (val instance = child.instance) {
            is AddMealSheetBloc.Child.RecipePicker -> RecipePickerScreen(instance.bloc)
            is AddMealSheetBloc.Child.MealPlanForm -> AddToMealPlanScreen(instance.bloc)
        }
    }
}
