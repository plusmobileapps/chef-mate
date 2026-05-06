@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.ui.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText

/**
 * Base class for Compose UI test robots.
 *
 * Robots wrap a [ComposeUiTest] and expose readable methods that encapsulate the selectors and wait
 * conditions for a single screen, so test code reads as a sequence of user actions rather than node
 * lookups. Methods should use [apply] to return the robot for fluent chaining.
 *
 * Driven from `runComposeUiTest { Robot(this)... }`. The same robot works for per-screen
 * multiplatform tests on JVM, iOS simulator, and Android emulator.
 */
abstract class Robot(protected val test: ComposeUiTest) {

    protected fun waitUntilTextDisplayed(text: String, timeoutMs: Long = DEFAULT_TIMEOUT_MS) {
        waitUntilAtLeastOneExists(hasText(text, substring = true), timeoutMs)
    }

    protected fun waitUntilAtLeastOneExists(
        matcher: SemanticsMatcher,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
    ) {
        test.waitUntil(timeoutMillis = timeoutMs) {
            test.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS: Long = 5_000
    }
}
