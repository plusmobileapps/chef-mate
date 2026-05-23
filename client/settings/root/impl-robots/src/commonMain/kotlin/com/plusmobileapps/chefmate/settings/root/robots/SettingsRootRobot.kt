@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.settings.root.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.settings.root.SettingsRootTestTags

/**
 * Robot for interacting with and asserting on the Settings root master-detail screen.
 *
 * Every node lookup is scoped to a descendant of [SettingsRootTestTags.SCREEN] so labels rendered
 * elsewhere (e.g. the bottom-nav Settings tab row) don't satisfy matchers.
 *
 * Construct via [settingsRoot] inside a `runComposeUiTest { … }` block.
 */
class SettingsRootRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(SettingsRootTestTags.SCREEN))

    fun awaitDisplayed(): SettingsRootRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(SettingsRootTestTags.SCREEN))
    }

    fun clickRow(label: String): SettingsRootRobot = apply {
        test.onNode(hasText(label) and onScreen).assertIsDisplayed().performClick()
    }

    fun assertRowDisplayed(label: String): SettingsRootRobot = apply {
        test.onNode(hasText(label) and onScreen).assertIsDisplayed()
    }

    fun awaitDetailsDisplayed(): SettingsRootRobot = apply {
        test.waitUntilExactlyOneExists(
            hasTestTag(SettingsRootTestTags.DETAILS_PANE) and
                hasAnyAncestor(hasTestTag(SettingsRootTestTags.SCREEN))
        )
    }

    fun assertMasterPaneVisible(): SettingsRootRobot = apply {
        test
            .onNode(
                hasTestTag(SettingsRootTestTags.MASTER_PANE) and
                    hasAnyAncestor(hasTestTag(SettingsRootTestTags.SCREEN))
            )
            .assertIsDisplayed()
    }

    fun assertNoDetails(): SettingsRootRobot = apply {
        test
            .onNode(
                hasTestTag(SettingsRootTestTags.DETAILS_PANE) and
                    hasAnyAncestor(hasTestTag(SettingsRootTestTags.SCREEN))
            )
            .assertIsNotDisplayed()
    }
}

fun ComposeUiTest.settingsRoot(): SettingsRootRobot = SettingsRootRobot(this)
