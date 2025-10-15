package com.plusmobileapps.chefmate.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.grocery.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.grocery.list.GroceryListBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc

interface RootBloc {
    val state: Value<ChildStack<*, Child>>

    sealed class Child {
        data class BottomNavigation(
            val bloc: BottomNavBloc,
        ) : Child()

        data class GroceryDetail(
            val bloc: GroceryDetailBloc,
        ) : Child()
    }

    fun interface Factory {
        fun create(context: BlocContext): RootBloc
    }
}
