package com.plusmobileapps.chefmate.root

import com.plusmobileapps.chefmate.ApplicationComponent
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.grocerylist.detail.GroceryDetailBlocImpl
import com.plusmobileapps.chefmate.grocerylist.list.GroceryListBlocImpl

object RootBlocFactory {
    fun create(
        context: BlocContext,
        appComponent: ApplicationComponent,
    ): RootBloc {
        return RootBlocImpl(
            context = context,
            groceryListBloc = { ctx, output ->
                GroceryListBlocImpl(
                    context = ctx,
                    repository = appComponent.groceryRepository,
                    output = output,
                )
            },
            groceryDetail = { ctx, itemId, output ->
                GroceryDetailBlocImpl(
                    context = ctx,
                    id = itemId,
                    repository = appComponent.groceryRepository,
                    output = output,
                )
            }
        )
    }
}