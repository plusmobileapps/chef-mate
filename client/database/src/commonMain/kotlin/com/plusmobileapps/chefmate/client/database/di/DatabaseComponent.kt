package com.plusmobileapps.chefmate.client.database.di

import com.plusmobileapps.chefmate.client.database.DriverFactory
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.GroceryQueries
import com.plusmobileapps.chefmate.database.RecipeQueries
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@SingleIn(AppScope::class)
@ContributesTo(AppScope::class)
interface DatabaseComponent {
    @SingleIn(AppScope::class)
    @Provides
    fun database(driverFactory: DriverFactory): Database = Database.Companion.invoke(driverFactory.createDriver())

    @SingleIn(AppScope::class)
    @Provides
    fun providesGroceryQueries(database: Database): GroceryQueries = database.groceryQueries

    @SingleIn(AppScope::class)
    @Provides
    fun providesRecipeQueries(database: Database): RecipeQueries = database.recipeQueries
}
