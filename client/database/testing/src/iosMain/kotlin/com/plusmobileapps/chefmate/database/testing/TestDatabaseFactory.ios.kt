package com.plusmobileapps.chefmate.database.testing

import app.cash.sqldelight.driver.native.inMemoryDriver
import com.plusmobileapps.chefmate.database.Database

actual fun createTestDatabase(): Database {
    val driver = inMemoryDriver(Database.Schema)
    driver.execute(null, "PRAGMA foreign_keys = ON", 0)
    return Database(driver)
}
