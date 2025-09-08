package com.plusmobileapps.chefmate.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.grocerylist.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.grocerylist.list.GroceryListBloc

interface RootBloc {

    val state: Value<ChildStack<*, Child>>

    sealed class Child {
        data class GroceryList(val bloc: GroceryListBloc) : Child()

        data class GroceryDetail(val bloc: GroceryDetailBloc) : Child()
    }
}