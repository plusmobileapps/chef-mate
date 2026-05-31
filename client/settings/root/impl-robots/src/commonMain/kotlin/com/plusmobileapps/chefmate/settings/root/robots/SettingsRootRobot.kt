@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.settings.root.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.settings.root.SettingsRootTestTags

/**
 * Robot for the settings-root navigation container. Every node lookup is scoped under
 * [SettingsRootTestTags.SCREEN] so a row label here never matches a like-named node on another
 * screen (e.g. the bottom-nav bar's own tab labels).
 */
class SettingsRootRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(SettingsRootTestTags.SCREEN))

    fun awaitDisplayed(): SettingsRootRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(SettingsRootTestTags.SCREEN))
    }

    fun assertRowDisplayed(label: String): SettingsRootRobot = apply {
        test.waitUntilExactlyOneExists(hasText(label) and onScreen)
    }

    fun clickRow(label: String): SettingsRootRobot = apply {
        test.waitUntilExactlyOneExists(hasText(label) and onScreen)
        test.onNode(hasText(label) and onScreen).performClick()
    }
}

fun ComposeUiTest.settingsRoot(): SettingsRootRobot = SettingsRootRobot(this)
