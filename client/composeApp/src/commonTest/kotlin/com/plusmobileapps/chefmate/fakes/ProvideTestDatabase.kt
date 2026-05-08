package com.plusmobileapps.chefmate.fakes

import com.plusmobileapps.chefmate.database.Database

/**
 * Per-platform test database used by [FakeDatabase]. JVM and iOS delegate to the existing
 * `createTestDatabase()` (JdbcSqliteDriver / NativeSqliteDriver). Android instrumented tests use
 * `AndroidSqliteDriver` with the test app context, since `JdbcSqliteDriver` would try to load
 * `libsqlitejdbc.so` and fail on a real device.
 */
expect fun provideTestDatabase(): Database
