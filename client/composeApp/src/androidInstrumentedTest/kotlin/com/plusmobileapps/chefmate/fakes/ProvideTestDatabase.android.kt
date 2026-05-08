package com.plusmobileapps.chefmate.fakes

import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.plusmobileapps.chefmate.database.Database

actual fun provideTestDatabase(): Database {
    val driver =
        AndroidSqliteDriver(
            schema = Database.Schema,
            context = ApplicationProvider.getApplicationContext(),
            name = null, // in-memory
        )
    return Database(driver)
}
