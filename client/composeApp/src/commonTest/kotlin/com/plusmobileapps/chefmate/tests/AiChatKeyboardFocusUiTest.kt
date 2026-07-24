@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import com.plusmobileapps.chefmate.aichat.AiChatTestTags
import com.plusmobileapps.chefmate.aichat.robots.aiChat
import com.plusmobileapps.chefmate.featureflag.FeatureFlagRegistry
import com.plusmobileapps.chefmate.fixtures.TestRecipes
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.core.robots.recipeDetail
import com.plusmobileapps.chefmate.recipe.list.robots.recipeList
import kotlin.test.Test

/**
 * Regression coverage for the peek -> expanded transition losing keyboard focus. The peek and
 * expanded presentations render structurally different `AiChatInput` composables, so the peek's
 * `OutlinedTextField` (and its keyboard) is disposed when focusing it expands the sheet, leaving
 * the freshly-composed expanded input unfocused until the user taps it a second time. See the
 * `LocalAiChatInputFocusRequester` comment in AiChatPresentation.kt and the LaunchedEffect in
 * RootScreen.kt's AiChatSheet for the fix.
 */
@OptIn(ExperimentalTestApi::class)
class AiChatKeyboardFocusUiTest {

    @Test
    fun focusing_peek_input_keeps_it_focused_after_expanding() = runRootBlocTest {
        it.testFeatureFlags.set(FeatureFlagRegistry.AiChat, true)

        recipeList().clickRecipe(TestRecipes.fullyPopulated.title)
        recipeDetail().awaitDisplayed().tapAiChat()

        aiChat().awaitPeekShown().expandFromPeek().awaitExpanded()
        waitForIdle()

        onNode(
                hasTestTag(AiChatTestTags.INPUT) and
                    hasAnyAncestor(hasTestTag(AiChatTestTags.SCREEN))
            )
            .assertIsFocused()
    }
}
