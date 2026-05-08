package com.plusmobileapps.chefmate.fakes

import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.testing.createTestDatabase

actual fun provideTestDatabase(): Database = createTestDatabase()
