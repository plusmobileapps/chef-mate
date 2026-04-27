package com.plusmobileapps.chefmate.client.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.plusmobileapps.chefmate.database.Database
import java.io.File
import java.util.Properties

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbPath = getAppDataDirectory()
        dbPath.mkdirs()
        val dbFile = File(dbPath, "chefmate.db")

        return JdbcSqliteDriver(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            properties = Properties(),
            schema = Database.Schema,
        )
    }

    private fun getAppDataDirectory(): File {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")

        return when {
            os.contains("mac") -> File(userHome, "Library/Application Support/Chef Mate")
            os.contains("win") -> File(System.getenv("APPDATA") ?: userHome, "Chef Mate")
            else -> File(userHome, ".local/share/chef-mate") // Linux
        }
    }
}
