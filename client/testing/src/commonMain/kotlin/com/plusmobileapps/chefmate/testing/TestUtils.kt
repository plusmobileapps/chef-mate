package com.plusmobileapps.chefmate.testing

import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

/**
 * Extension function for FunSpec to run suspend tests without explicitly using runTest.
 *
 * Usage:
 * ```
 * suspendTest("my test description") {
 *     // Your suspend test code here
 * }
 * ```
 */
fun FunSpec.suspendTest(
    name: String,
    test: suspend TestScope.() -> Unit,
) {
    test(name) {
        runTest {
            test()
        }
    }
}
