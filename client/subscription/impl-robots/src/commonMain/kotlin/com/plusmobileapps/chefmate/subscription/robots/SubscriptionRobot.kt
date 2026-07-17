@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.subscription.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.subscription.SubscriptionTestTags

/**
 * Robot for the paywall
 * [SubscriptionScreen][com.plusmobileapps.chefmate.subscription.SubscriptionBloc]. Node lookups are
 * scoped under [SubscriptionTestTags.SCREEN].
 */
class SubscriptionRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(SubscriptionTestTags.SCREEN))

    fun awaitDisplayed(): SubscriptionRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(SubscriptionTestTags.SCREEN))
    }

    fun selectPackage(packageId: String): SubscriptionRobot = apply {
        val tag = hasTestTag(SubscriptionTestTags.PACKAGE_ROW_PREFIX + packageId)
        test.waitUntilExactlyOneExists(tag)
        test.onNode(tag).performClick()
    }

    fun clickSubscribe(): SubscriptionRobot = apply {
        test.onNode(hasTestTag(SubscriptionTestTags.SUBSCRIBE_BUTTON) and onScreen).performClick()
    }

    fun clickRestore(): SubscriptionRobot = apply {
        test.onNode(hasTestTag(SubscriptionTestTags.RESTORE_BUTTON) and onScreen).performClick()
    }
}

fun ComposeUiTest.subscription(): SubscriptionRobot = SubscriptionRobot(this)
