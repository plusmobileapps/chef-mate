@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.featureflag.FeatureFlagRegistry
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.bottomnav.robots.bottomNav
import com.plusmobileapps.chefmate.settings.robots.more
import com.plusmobileapps.chefmate.subscription.robots.subscription
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubscriptionNavigationUiTest {

    @Test
    fun more_tab_premium_row_opens_paywall() = runRootBlocTest {
        bottomNav().clickMoreTab()
        more().awaitDisplayed().clickSubscriptionRow()

        subscription().awaitDisplayed()
    }

    @Test
    fun ai_chat_when_not_premium_shows_gate_dialog_then_paywall_on_confirm() =
        runRootBlocTest { component ->
            // AI chat is enabled but the user is not premium (the test repo's default), so tapping
            // it
            // must surface the premium gate rather than the chat.
            component.testFeatureFlags.set(FeatureFlagRegistry.AiChat, true)

            bottomNav().clickMoreTab()
            more().awaitDisplayed().clickAiChatRow()

            // Gate dialog appears; confirming ("See Plans") opens the paywall.
            waitUntilExactlyOneExists(hasText("See Plans"))
            onNode(hasText("See Plans")).performClick()

            subscription().awaitDisplayed()
        }
}
