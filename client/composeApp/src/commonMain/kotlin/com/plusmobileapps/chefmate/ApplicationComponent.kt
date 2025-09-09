package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.grocerylist.GroceryRepository
import com.plusmobileapps.chefmate.root.RootBloc

interface ApplicationComponent {
    val rootBlocFactory: RootBloc.Factory
}