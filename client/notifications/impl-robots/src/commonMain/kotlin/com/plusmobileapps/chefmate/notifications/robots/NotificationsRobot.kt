@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.notifications.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.notifications.NotificationsTestTags

/**
 * Robot for the Notifications screen. Lookups are scoped to a descendant of
 * [NotificationsTestTags.SCREEN] so controls rendered on other screens don't satisfy matchers.
 *
 * Construct via [notifications] from inside a `runComposeUiTest { … }` block.
 */
class NotificationsRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(NotificationsTestTags.SCREEN))

    fun awaitDisplayed(): NotificationsRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(NotificationsTestTags.SCREEN))
    }

    fun assertDisplayed(): NotificationsRobot = apply {
        test.onNode(hasTestTag(NotificationsTestTags.SCREEN)).assertIsDisplayed()
    }

    fun tapAccept(notificationKey: String): NotificationsRobot = apply {
        test
            .onNode(hasTestTag(NotificationsTestTags.ACCEPT_PREFIX + notificationKey) and onScreen)
            .performClick()
    }

    fun tapDecline(notificationKey: String): NotificationsRobot = apply {
        test
            .onNode(hasTestTag(NotificationsTestTags.DECLINE_PREFIX + notificationKey) and onScreen)
            .performClick()
    }

    fun assertEmpty(): NotificationsRobot = apply {
        test.onNode(hasTestTag(NotificationsTestTags.EMPTY) and onScreen).assertIsDisplayed()
    }
}

fun ComposeUiTest.notifications(): NotificationsRobot = NotificationsRobot(this)
