package com.plusmobileapps.chefmate.root

import com.plusmobileapps.chefmate.ApplicationComponent
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.grocerylist.GroceryListBlocImpl

object RootBlocFactory {
    fun create(
        context: BlocContext,
        appComponent: ApplicationComponent,
    ): RootBloc {
        return RootBlocImpl(
            context = context,
            groceryListBloc = { ctx ->
                GroceryListBlocImpl(
                    context = ctx,
                    repository = appComponent.groceryRepository,
                )
            },
        )
    }
}