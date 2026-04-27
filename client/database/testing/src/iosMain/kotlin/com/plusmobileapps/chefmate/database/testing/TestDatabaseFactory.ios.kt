package com.plusmobileapps.chefmate.database.testing

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.plusmobileapps.chefmate.database.Database

actual fun createTestDatabase(): Database {
    val driver = NativeSqliteDriver(Database.Schema, null)
    return Database(driver)
}
