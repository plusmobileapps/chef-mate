package com.plusmobileapps.chefmate.recipe.core.addmeal

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.serialization.Serializable

interface MealPlannerRootBloc : BackHandlerOwner, BackClickBloc, BlocScreen {
    val routerState: Value<ChildStack<*, Child>>

    sealed class Child {
        data class RecipePicker(val bloc: RecipePickerBloc) : Child(), BlocScreen by bloc

        data class ChooseDate(val bloc: ChooseDateBloc) : Child(), BlocScreen by bloc

        data class ChooseMealType(val bloc: ChooseMealTypeBloc) : Child(), BlocScreen by bloc
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
