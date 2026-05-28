@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.aichat.robots.aiChat
import com.plusmobileapps.chefmate.featureflag.FeatureFlagRegistry
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AiChatNavigationUiTest {

    private val extractedRecipe =
        ExtractedRecipeData(
            title = "Lemon-Roasted Chicken Thighs",
            description = "From the chat",
            ingredients = listOf("4 chicken thighs", "2 lemons", "1 tbsp olive oil"),
            directions = listOf("Sear skin-side down", "Roast at 425°F for 25 minutes"),
            imageUrl = null,
            sourceUrl = "",
            servings = 4,
            prepTime = 5,
            cookTime = 25,
            totalTime = 30,
            calories = null,
        )

    @Test
    fun add_recipe_pill_opens_edit_form_pre_filled_with_extracted_recipe() =
        runRootBlocTest { component ->
            component.testFeatureFlags.set(FeatureFlagRegistry.AiChat, true)
            component.fakeGeminiClient.deltas = listOf("Try lemon-roasted chicken thighs!")
            component.fakeGeminiRecipeExtractor.response = extractedRecipe

            // Open More → AI Chat.
            waitUntilExactlyOneExists(hasText("More"))
            onNode(hasText("More")).performClick()
            waitUntilExactlyOneExists(hasText("AI Chat"))
            onNode(hasText("AI Chat")).performClick()

            // Send a message; the fake gemini replies with one canned delta.
            aiChat()
                .typeMessage("What can I do with chicken thighs?")
                .tapSend()
                .awaitMessageShown("Try lemon-roasted chicken thighs!")

            // Pill becomes available once the model reply is finished.
            aiChat().tapAddRecipe()

            // Land on the Edit Recipe screen pre-filled with Gemini's extracted recipe.
            waitUntilExactlyOneExists(hasText(extractedRecipe.title))
            onNode(hasText(extractedRecipe.title)).assertIsDisplayed()
        }
}
