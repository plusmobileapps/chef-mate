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
import kotlin.test.assertTrue

/**
 * Regression coverage for the collapsed sheet "peek": [AiChatSheetNavigationUiTest] only asserts
 * that the peek node exists in the semantics tree, which doesn't catch the sheet's *measured*
 * height being wrong. ModalBottomSheet's Dialog measures its content with a tight constraint
 * matching the full window (see the comment on the Box in RootScreen.kt's AiChatSheet), so without
 * relaxing that constraint the peek silently renders at full window height with the input pinned
 * near the top, even though every node the navigation test checks for is still present.
 */
@OptIn(ExperimentalTestApi::class)
class AiChatSheetPeekSizingUiTest {

    @Test
    fun peek_sheet_hugs_its_content_height_instead_of_filling_the_window() = runRootBlocTest {
        it.testFeatureFlags.set(FeatureFlagRegistry.AiChat, true)

        recipeList().clickRecipe(TestRecipes.fullyPopulated.title)
        recipeDetail().awaitDisplayed().tapAiChat()
        aiChat().awaitPeekShown()

        val peekHeight = onNodeWithTag(AiChatTestTags.PEEK).fetchSemanticsNode().size.height
        val windowHeight = onNodeWithTag(AiChatTestTags.PEEK).fetchSemanticsNode().boundsInWindow

        // The peek is just a recipe chip + input row; it should be a small strip, nowhere close to
        // filling the test window's height. This is a regression guard, not a pixel-exact bound.
        assertTrue(
            peekHeight < 400,
            "expected the collapsed AI chat peek to hug its small content height, " +
                "but it measured $peekHeight px tall (window bounds: $windowHeight)",
        )
    }
}
