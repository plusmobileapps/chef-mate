package com.plusmobileapps.chefmate.di

import android.content.Context
import com.plusmobileapps.chefmate.ApplicationComponent
import com.plusmobileapps.chefmate.client.database.DriverFactory
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.GroceryQueries
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
abstract class AndroidApplicationComponent(
    @get:Provides val context: Context
) : ApplicationComponent {

    @Provides
    fun driverFactory(): DriverFactory = DriverFactory(context = context)

    @SingleIn(AppScope::class)
    @Provides
    fun database(driverFactory: DriverFactory): Database =
        Database.Companion.invoke(driverFactory.createDriver())

    @SingleIn(AppScope::class)
    @Provides
    fun providesGroceryQueries(database: Database): GroceryQueries = database.groceryQueries

}