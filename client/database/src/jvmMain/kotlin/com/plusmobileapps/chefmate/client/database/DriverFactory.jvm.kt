package com.plusmobileapps.chefmate.client.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.plusmobileapps.chefmate.database.Database
import java.util.Properties

actual class DriverFactory {
    actual fun createDriver(): SqlDriver =
        JdbcSqliteDriver(
            url = "jdbc:sqlite:chefmate.db",
            properties = Properties(),
            schema = Database.Schema,
        )
}
