@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.profile.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.profile.ProfileTestTags

/**
 * Robot for the public profile screen. Every lookup is scoped to a descendant of
 * [ProfileTestTags.SCREEN] so content rendered on other screens can't satisfy a matcher.
 *
 * Construct via [profile] from inside a `runComposeUiTest { … }` block.
 */
class ProfileRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(ProfileTestTags.SCREEN))

    fun awaitDisplayed(): ProfileRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(ProfileTestTags.SCREEN))
    }

    fun awaitLoaded(): ProfileRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(ProfileTestTags.HANDLE) and onScreen)
    }

    fun assertHandle(handle: String): ProfileRobot = apply {
        test.onNode(hasTestTag(ProfileTestTags.HANDLE) and onScreen).assertTextEquals("@$handle")
    }

    fun assertDisplayName(name: String): ProfileRobot = apply {
        test.onNode(hasTestTag(ProfileTestTags.DISPLAY_NAME) and onScreen).assertTextEquals(name)
    }

    fun assertBio(bio: String): ProfileRobot = apply {
        test.onNode(hasTestTag(ProfileTestTags.BIO) and onScreen).assertTextEquals(bio)
    }

    fun assertRecipeCount(count: Int): ProfileRobot = apply {
        test
            .onAllNodes(hasTestTag(ProfileTestTags.RECIPE_ITEM) and onScreen)
            .assertCountEquals(count)
    }

    fun tapFirstRecipe(): ProfileRobot = apply {
        test
            .onAllNodes(hasTestTag(ProfileTestTags.RECIPE_ITEM) and onScreen)
            .onFirst()
            .performClick()
    }

    fun tapManageProfile(): ProfileRobot = apply {
        test.onNode(hasTestTag(ProfileTestTags.MANAGE) and onScreen).performClick()
    }

    fun tapCreateProfile(): ProfileRobot = apply {
        test.onNode(hasTestTag(ProfileTestTags.CREATE_PROFILE) and onScreen).performClick()
    }

    fun tapRetry(): ProfileRobot = apply {
        test.onNode(hasTestTag(ProfileTestTags.RETRY) and onScreen).performClick()
    }

    /** The "you haven't published anything" / "create a profile" empty state. */
    fun assertEmptyStateShown(): ProfileRobot = apply {
        test.onNode(hasTestTag(ProfileTestTags.EMPTY) and onScreen).assertIsDisplayed()
    }

    fun assertNotFoundShown(): ProfileRobot = apply {
        test.onNode(hasTestTag(ProfileTestTags.NOT_FOUND) and onScreen).assertIsDisplayed()
    }

    fun assertOfflineShown(): ProfileRobot = apply {
        test.onNode(hasTestTag(ProfileTestTags.OFFLINE) and onScreen).assertIsDisplayed()
    }

    fun assertDisplayed(): ProfileRobot = apply {
        test.onNode(hasTestTag(ProfileTestTags.SCREEN)).assertIsDisplayed()
    }
}

fun ComposeUiTest.profile(): ProfileRobot = ProfileRobot(this)
