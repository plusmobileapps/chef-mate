package com.plusmobileapps.chefmate.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc

interface RootBloc :
    BackHandlerOwner,
    BackClickBloc {
    val state: Value<ChildStack<*, Child>>

    sealed class Child {
        data class BottomNavigation(
            val bloc: BottomNavBloc,
        ) : Child()

        data class GroceryDetail(
            val bloc: GroceryDetailBloc,
        ) : Child()

        data class RecipeRoot(
            val bloc: RecipeRootBloc,
        ) : Child()

        data class Authentication(
            val bloc: AuthenticationBloc,
        ) : Child()
    }

    fun interface Factory {
        fun create(context: BlocContext): RootBloc
    }
}
