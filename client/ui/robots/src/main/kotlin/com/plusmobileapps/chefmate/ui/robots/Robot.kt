@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.ui.robots

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * Base class for Compose UI test robots.
 *
 * Robots wrap a [ComposeContentTestRule] and expose readable methods that encapsulate the selectors
 * and wait conditions for a single screen, so test code reads as a sequence of user actions rather
 * than node lookups. Methods should use [apply] to return the robot for fluent chaining.
 */
abstract class Robot(protected val rule: ComposeContentTestRule) {

    protected fun waitUntilTextDisplayed(text: String, timeoutMs: Long = DEFAULT_TIMEOUT_MS) {
        waitUntilAtLeastOneExists(hasText(text, substring = true), timeoutMs)
    }

    protected fun waitUntilAtLeastOneExists(
        matcher: SemanticsMatcher,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
    ) {
        rule.waitUntil(timeoutMs) { rule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS: Long = 5_000
    }
}
