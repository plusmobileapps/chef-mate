package com.plusmobileapps.chefmate.database.testing

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.plusmobileapps.chefmate.database.Database

actual fun createTestDatabase(): Database {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    Database.Schema.create(driver)
    return Database(driver)
}
