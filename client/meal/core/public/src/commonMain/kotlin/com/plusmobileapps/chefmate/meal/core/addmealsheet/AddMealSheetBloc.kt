package com.plusmobileapps.chefmate.meal.core.addmealsheet

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.meal.core.recipepicker.RecipePickerBloc
import com.plusmobileapps.chefmate.recipe.core.addmeal.AddToMealPlanBloc

interface AddMealSheetBloc : BackHandlerOwner, BackClickBloc {
    val routerState: Value<ChildStack<*, Child>>

    sealed class Child {
        data class RecipePicker(val bloc: RecipePickerBloc) : Child()

        data class MealPlanForm(val bloc: AddToMealPlanBloc) : Child()
    }

    sealed class Output {
        data object Finished : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): AddMealSheetBloc
    }
}
