@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.aichat.robots.aiChat
import com.plusmobileapps.chefmate.featureflag.FeatureFlagRegistry
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.bottomnav.robots.bottomNav
import com.plusmobileapps.chefmate.recipe.core.robots.editRecipe
import com.plusmobileapps.chefmate.recipe.list.robots.recipeList
import com.plusmobileapps.chefmate.settings.robots.more
import kotlin.test.Test

/**
 * Covers the two entry points for "create a recipe from a photo": the recipe-list add-recipe
 * chooser and the AI-chat attach-photo button. The platform image picker can't be driven inside
 * `ComposeUiTest`, so these flows assert the affordances are wired up; the extraction → editor
 * navigation itself is covered by unit tests on the BLoCs.
 */
@OptIn(ExperimentalTestApi::class)
class RecipeFromPhotoUiTest {

    @Test
    fun add_menu_offers_scan_from_photo_and_create_opens_editor() = runRootBlocTest {
        recipeList().openAddMenu().assertScanFromPhotoShown().tapCreateRecipe()

        editRecipe().awaitDisplayed()
    }

    @Test
    fun ai_chat_shows_attach_photo_button() = runRootBlocTest { component ->
        component.testFeatureFlags.set(FeatureFlagRegistry.AiChat, true)

        bottomNav().clickMoreTab()
        more().awaitDisplayed().clickAiChatRow()

        aiChat().assertAttachPhotoShown()
    }
}
