package com.plusmobileapps.chefmate.client.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.plusmobileapps.chefmate.database.Database

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "chefmate.db",
            callback =
                object : AndroidSqliteDriver.Callback(Database.Schema) {
                    override fun onConfigure(db: SupportSQLiteDatabase) {
                        super.onConfigure(db)
                        db.setForeignKeyConstraintsEnabled(true)
                    }
                },
        )
}
