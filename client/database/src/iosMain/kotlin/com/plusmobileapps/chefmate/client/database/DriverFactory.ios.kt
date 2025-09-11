package com.plusmobileapps.chefmate.client.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.plusmobileapps.chefmate.database.Database

actual class DriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(
            schema = Database.Schema,
            name = "chefmate.db",
        )
}
