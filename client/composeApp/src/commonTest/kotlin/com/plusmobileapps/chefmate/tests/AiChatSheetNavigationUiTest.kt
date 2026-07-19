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

@OptIn(ExperimentalTestApi::class)
class AiChatSheetNavigationUiTest {

    /**
     * Opening the AI chat from a recipe shows it as a collapsed sheet "peek" (input over the
     * recipe), and focusing the input expands it to the full-screen chat — the recipe detail
     * underneath is never replaced by a full-screen chat child.
     */
    @Test
    fun ai_chat_from_recipe_detail_opens_as_a_peek_then_expands() = runRootBlocTest { component ->
        component.testFeatureFlags.set(FeatureFlagRegistry.AiChat, true)

        recipeList().clickRecipe(TestRecipes.fullyPopulated.title)
        recipeDetail().awaitDisplayed().tapAiChat()

        aiChat().awaitPeekShown().expandFromPeek().awaitExpanded()
    }
}
