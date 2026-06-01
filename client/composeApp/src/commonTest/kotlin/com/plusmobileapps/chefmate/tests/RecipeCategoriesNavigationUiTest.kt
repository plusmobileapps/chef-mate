package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.bottomnav.robots.bottomNav
import com.plusmobileapps.chefmate.recipe.categories.robots.recipeCategories
import com.plusmobileapps.chefmate.settings.robots.more
import com.plusmobileapps.chefmate.settings.root.robots.settingsRoot
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RecipeCategoriesNavigationUiTest {

    @Test
    fun opening_settings_then_categories_lands_on_recipe_categories_screen() = runRootBlocTest {
        bottomNav().clickMoreTab()
        more().awaitDisplayed().clickAppSettingsRow()

        settingsRoot().awaitDisplayed().clickRow("Categories")

        // Built-in presets always show on the categories screen (even with zero recipes), so
        // "Breakfast" is a stable signal that the new management screen rendered.
        recipeCategories().awaitDisplayed().assertCategoryDisplayed("Breakfast")
    }
}
