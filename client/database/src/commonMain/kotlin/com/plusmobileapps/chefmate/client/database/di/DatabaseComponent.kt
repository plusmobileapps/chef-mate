package com.plusmobileapps.chefmate.client.database.di

import com.plusmobileapps.chefmate.client.database.DriverFactory
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.GroceryListMemberQueries
import com.plusmobileapps.chefmate.database.GroceryListQueries
import com.plusmobileapps.chefmate.database.GroceryQueries
import com.plusmobileapps.chefmate.database.MealPlanQueries
import com.plusmobileapps.chefmate.database.RecipeQueries
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesTo(AppScope::class)
interface DatabaseComponent {
    @SingleIn(AppScope::class)
    @Provides
    fun database(driverFactory: DriverFactory): Database =
        Database.Companion.invoke(driverFactory.createDriver())

    @SingleIn(AppScope::class)
    @Provides
    fun providesGroceryQueries(database: Database): GroceryQueries = database.groceryQueries

    @SingleIn(AppScope::class)
    @Provides
    fun providesGroceryListQueries(database: Database): GroceryListQueries =
        database.groceryListQueries

    @SingleIn(AppScope::class)
    @Provides
    fun providesMealPlanQueries(database: Database): MealPlanQueries = database.mealPlanQueries

    @SingleIn(AppScope::class)
    @Provides
    fun providesRecipeQueries(database: Database): RecipeQueries = database.recipeQueries

    @SingleIn(AppScope::class)
    @Provides
    fun providesGroceryListMemberQueries(database: Database): GroceryListMemberQueries =
        database.groceryListMemberQueries
}
