package com.plusmobileapps.chefmate.recipe.core.addmeal

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import kotlinx.serialization.Serializable

interface MealPlannerRootBloc : BackHandlerOwner, BackClickBloc {
    val routerState: Value<ChildStack<*, Child>>

    sealed class Child {
        data class RecipePicker(val bloc: RecipePickerBloc) : Child()

        data class ChooseDate(val bloc: ChooseDateBloc) : Child()

        data class ChooseMealType(val bloc: ChooseMealTypeBloc) : Child()
    }

    sealed class Output {
        data object Finished : Output()
    }

    @Serializable
    sealed class Props {
        @Serializable data class FromRecipeDetail(val recipeId: Long) : Props()

        @Serializable data object FromMealPlanner : Props()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            props: Props,
            output: Consumer<Output>,
        ): MealPlannerRootBloc
    }
}
