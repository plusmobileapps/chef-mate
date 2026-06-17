package com.plusmobileapps.chefmate.recipe.core.addmeal

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.serialization.Serializable

interface MealPlannerRootBloc : BackHandlerOwner, BackClickBloc, ComposeScreen {
    val routerState: Value<ChildStack<*, Child>>

    sealed class Child {

        abstract val bloc: ComposeScreen

        data class RecipePicker(override val bloc: RecipePickerBloc) : Child()

        data class ChooseDate(override val bloc: ChooseDateBloc) : Child()

        data class ChooseMealType(override val bloc: ChooseMealTypeBloc) : Child()
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
