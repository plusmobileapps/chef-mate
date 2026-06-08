package com.plusmobileapps.chefmate.util.testing

import com.plusmobileapps.chefmate.util.Unique

/**
 * Deterministic [Unique] for tests. Emits "$prefix-1", "$prefix-2", … on successive calls so tests
 * can assert on the generated ids instead of fighting random UUIDs.
 */
class FakeUnique(private val prefix: String = "uid") : Unique {
    private var counter = 0

    override fun generate(): String = "$prefix-${++counter}"
}
