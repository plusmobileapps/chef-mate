@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import com.plusmobileapps.chefmate.aichat.AiChatTestTags
import com.plusmobileapps.chefmate.aichat.robots.aiChat
import com.plusmobileapps.chefmate.featureflag.FeatureFlagRegistry
import com.plusmobileapps.chefmate.fixtures.TestRecipes
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.core.robots.recipeDetail
import com.plusmobileapps.chefmate.recipe.list.robots.recipeList
import kotlin.test.Test

/**
 * Opening the AI chat from a recipe reopens that recipe's most recent conversation (showing an
 * excerpt in the peek) instead of always starting fresh; the new-conversation button starts over.
 */
@OptIn(ExperimentalTestApi::class)
class AiChatRecipeReopenUiTest {

    @Test
    fun reopening_from_a_recipe_shows_the_prior_conversations_excerpt() =
        runRootBlocTest { component ->
            component.testFeatureFlags.set(FeatureFlagRegistry.AiChat, true)
            component.fakeGeminiClient.deltas = listOf("Bake it at 400°F for 25 minutes.")

            recipeList().clickRecipe(TestRecipes.fullyPopulated.title)
            recipeDetail().awaitDisplayed().tapAiChat()

            // Start a conversation for this recipe, then close the sheet.
            aiChat()
                .awaitPeekShown()
                .typeInPeek("How long do I bake it?")
                .sendFromPeek()
                .awaitExpanded()
            aiChat().awaitMessageShown("Bake it at 400").closeFromExpanded()

            // Reopening from the same recipe reopens that conversation: the peek shows its excerpt.
            recipeDetail().awaitDisplayed().tapAiChat()
            aiChat()
                .awaitPeekShown()
                .awaitPeekExcerptShown()
                .assertPeekShowsText("How long do I bake it?")
        }

    @Test
    fun new_conversation_button_clears_the_reopened_conversation() = runRootBlocTest { component ->
        component.testFeatureFlags.set(FeatureFlagRegistry.AiChat, true)
        component.fakeGeminiClient.deltas = listOf("Sure, here's how.")

        recipeList().clickRecipe(TestRecipes.fullyPopulated.title)
        recipeDetail().awaitDisplayed().tapAiChat()
        aiChat().awaitPeekShown().typeInPeek("First question?").sendFromPeek().awaitExpanded()
        aiChat().awaitMessageShown("Sure, here's how.").closeFromExpanded()

        // Reopen (excerpt present), then start a new conversation — the excerpt should disappear.
        recipeDetail().awaitDisplayed().tapAiChat()
        aiChat().awaitPeekShown().awaitPeekExcerptShown().tapNewChatFromPeek()

        aiChat().awaitPeekShown()
        waitForIdle()
        aiChat().assertStillPeek()
        onNodeWithTag(AiChatTestTags.PEEK_EXCERPT).assertDoesNotExist()
    }
}
