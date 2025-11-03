package com.plusmobileapps.chefmate.recipe.core.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc
import com.plusmobileapps.chefmate.recipe.core.edit.EditRecipeBloc
import kotlinx.serialization.Serializable

interface RecipeRootBloc {
    val routerState: Value<ChildStack<*, Child>>

    sealed class Child {
        data class Detail(
            val bloc: RecipeDetailBloc,
        ) : Child()

        data class Edit(
            val bloc: EditRecipeBloc,
        ) : Child()
    }

    sealed class Output {
        data object Finished : Output()
    }

    @Serializable
    sealed class Props {
        data class Detail(
            val recipeId: Long,
        ) : Props()

        data object Create : Props()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            props: Props,
            output: Consumer<Output>,
        ): RecipeRootBloc
    }
}
