@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.grocery.core.robots.editGroceryList
import com.plusmobileapps.chefmate.grocery.core.robots.groceryList
import com.plusmobileapps.chefmate.harness.TestUserState
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.bottomnav.robots.bottomNav
import kotlin.test.Test

class EditGroceryListNavigationUiTest {

    // "Edit list" is the localized content description of the per-row pencil icon
    // (grocery_edit_list).
    private val editListContentDescription = "Edit list"

    @Test
    fun editing_list_from_selector_opens_edit_screen_with_unauth_collaboration() =
        runRootBlocTest(userState = TestUserState.UnauthenticatedWithRecipes()) {
            bottomNav().clickGroceriesTab()

            groceryList().openListSelector().clickEditFirstList(editListContentDescription)

            // Edit screen opens; collaboration is gated behind sign in for a signed-out user.
            editGroceryList().awaitDisplayed().assertSignInShown()
        }
}
