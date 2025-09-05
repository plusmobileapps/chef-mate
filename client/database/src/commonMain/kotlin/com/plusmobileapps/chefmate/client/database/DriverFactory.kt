package com.plusmobileapps.chefmate.client.database

import app.cash.sqldelight.db.SqlDriver
import com.plusmobileapps.chefmate.database.Database

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): Database {
    val driver = driverFactory.createDriver()
    return Database(driver)
}