package com.plusmobileapps.chefmate.root

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.grocerylist.GroceryListBlocImpl

object RootBlocFactory {
    fun create(context: BlocContext): RootBloc {
        return RootBlocImpl(
            context = context,
            groceryListBloc = { ctx -> GroceryListBlocImpl(ctx) },
        )
    }
}