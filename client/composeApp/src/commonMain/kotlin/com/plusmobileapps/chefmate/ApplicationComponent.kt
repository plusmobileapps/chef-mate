package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.grocerylist.GroceryRepository

interface ApplicationComponent {
    val groceryRepository: GroceryRepository
}