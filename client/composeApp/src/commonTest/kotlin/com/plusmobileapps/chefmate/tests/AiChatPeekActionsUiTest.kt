@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.aichat.robots.aiChat
import com.plusmobileapps.chefmate.featureflag.FeatureFlagRegistry
import com.plusmobileapps.chefmate.fixtures.TestRecipes
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.core.robots.recipeDetail
import com.plusmobileapps.chefmate.recipe.list.robots.recipeList
import kotlin.test.Test

/**
 * The collapsed peek carries its own compact action bar (history + close) and drops the user
 * straight into typing by focusing the input as soon as the sheet opens.
 */
@OptIn(ExperimentalTestApi::class)
class AiChatPeekActionsUiTest {

    @Test
    fun peek_shows_history_and_close_and_focuses_input_on_open() = runRootBlocTest {
        it.testFeatureFlags.set(FeatureFlagRegistry.AiChat, true)

        recipeList().clickRecipe(TestRecipes.fullyPopulated.title)
        recipeDetail().awaitDisplayed().tapAiChat()

        aiChat().awaitPeekShown()
        waitForIdle()

        aiChat().assertPeekActionsShown().assertPeekInputFocused()
    }
}
