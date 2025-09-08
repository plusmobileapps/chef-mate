package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.client.database.DriverFactory
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.GroceryQueries
import com.plusmobileapps.chefmate.grocerylist.GroceryRepository
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

interface ApplicationComponent {
    val groceryRepository: GroceryRepository
}