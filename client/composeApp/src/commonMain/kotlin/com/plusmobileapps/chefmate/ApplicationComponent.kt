package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.client.database.DriverFactory
import com.plusmobileapps.chefmate.client.database.createDatabase
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.grocerylist.GroceryRepository
import com.plusmobileapps.chefmate.grocerylist.GroceryRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.coroutines.CoroutineContext

interface ApplicationComponent {
    val driverFactory: DriverFactory

    val database: Database

    val ioContext: CoroutineContext

    val groceryRepository: GroceryRepository
}

class DefaultApplicationComponent(
    override val driverFactory: DriverFactory,
) : ApplicationComponent {
    override val database: Database = createDatabase(driverFactory)

    override val ioContext: CoroutineContext
        get() = Dispatchers.IO

    override val groceryRepository: GroceryRepository = GroceryRepositoryImpl(
        queries = database.groceryQueries,
        ioContext = ioContext,
    )
}