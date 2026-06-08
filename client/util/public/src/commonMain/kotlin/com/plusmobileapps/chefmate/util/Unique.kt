package com.plusmobileapps.chefmate.util

/**
 * Generates unique identifiers (e.g. the client-side `clientId` used to dedup offline-first
 * creates). Abstracted behind an interface so call sites don't hardcode UUID generation — that
 * keeps the generated value injectable and deterministic in tests.
 */
fun interface Unique {
    fun generate(): String
}
