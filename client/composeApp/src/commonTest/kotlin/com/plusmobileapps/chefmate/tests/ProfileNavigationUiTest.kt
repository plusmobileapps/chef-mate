package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.profile.robots.manageProfile
import com.plusmobileapps.chefmate.profile.robots.profile
import com.plusmobileapps.chefmate.recipe.bottomnav.robots.bottomNav
import com.plusmobileapps.chefmate.settings.robots.more
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ProfileNavigationUiTest {

    @Test
    fun opening_my_profile_without_a_handle_shows_the_create_prompt() = runRootBlocTest {
        bottomNav().clickMoreTab()
        more().awaitDisplayed().clickMyProfileRow()

        // No handle claimed yet, so the profile invites the user to create one.
        profile().awaitDisplayed().assertEmptyStateShown()
    }

    @Test
    fun create_profile_routes_to_the_editor_to_claim_a_handle() = runRootBlocTest {
        bottomNav().clickMoreTab()
        more().awaitDisplayed().clickMyProfileRow()

        profile().awaitDisplayed().tapCreateProfile()

        manageProfile().awaitDisplayed().assertDisplayed()
    }

    @Test
    fun claiming_a_handle_from_the_editor_saves_and_returns() = runRootBlocTest {
        bottomNav().clickMoreTab()
        more().awaitDisplayed().clickMyProfileRow()
        profile().awaitDisplayed().tapCreateProfile()

        manageProfile()
            .awaitDisplayed()
            .setDisplayName("Julia Child")
            .setHandle("juliachild")
            .setBio("French cooking, demystified.")
            // The handle availability check is debounced, so Save unlocks a beat later.
            .awaitSaveEnabled()
            .tapSave()

        // Saving pops back to whatever launched the editor.
        profile().awaitDisplayed()
    }
}
