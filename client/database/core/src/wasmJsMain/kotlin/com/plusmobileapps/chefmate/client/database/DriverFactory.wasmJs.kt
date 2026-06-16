package com.plusmobileapps.chefmate.client.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Web (wasmJs) database driver — intentionally stubbed for the current web spike.
 *
 * The offline-first SQLDelight store is not yet wired for the browser. The real implementation
 * would use SQLDelight's web-worker driver (`app.cash.sqldelight:web-worker-driver` backed by
 * sql.js, plus a worker bundle and the schema's `await`-based create). Until that lands, any code
 * path that touches the local database on web will fail fast here.
 */
actual class DriverFactory {
    actual fun createDriver(): SqlDriver =
        throw NotImplementedError(
            "SQLDelight web (wasmJs) driver is not wired yet — deferred for the web spike."
        )
}
