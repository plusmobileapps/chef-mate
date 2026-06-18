package com.plusmobileapps.chefmate.onboarding.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.onboarding.CookModeBloc
import com.plusmobileapps.chefmate.onboarding.CookModeScreen
import com.plusmobileapps.chefmate.onboarding.GroceryListBloc
import com.plusmobileapps.chefmate.onboarding.GroceryListScreen
import com.plusmobileapps.chefmate.onboarding.MealPlanningBloc
import com.plusmobileapps.chefmate.onboarding.MealPlanningScreen
import com.plusmobileapps.chefmate.onboarding.SaveRecipesBloc
import com.plusmobileapps.chefmate.onboarding.SaveRecipesScreen
import com.plusmobileapps.chefmate.onboarding.StartCookingBloc
import com.plusmobileapps.chefmate.onboarding.StartCookingScreen
import com.plusmobileapps.chefmate.onboarding.WelcomeBloc
import com.plusmobileapps.chefmate.onboarding.WelcomeScreen
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// Public fake Blocs so the screenshot-test module can reuse them. The onboarding step screens are
// stateless, so the fakes only need to satisfy the interface.

val previewWelcomeBloc: WelcomeBloc =
    object : WelcomeBloc {
        override fun onGetStartedClicked() = Unit

        override fun onSignInClicked() = Unit

        override fun onSkipClicked() = Unit
    }

val previewSaveRecipesBloc: SaveRecipesBloc =
    object : SaveRecipesBloc {
        override fun onNextClicked() = Unit
    }

val previewCookModeBloc: CookModeBloc =
    object : CookModeBloc {
        override fun onNextClicked() = Unit
    }

val previewGroceryListBloc: GroceryListBloc =
    object : GroceryListBloc {
        override fun onNextClicked() = Unit
    }

val previewMealPlanningBloc: MealPlanningBloc =
    object : MealPlanningBloc {
        override fun onNextClicked() = Unit
    }

val previewStartCookingBloc: StartCookingBloc =
    object : StartCookingBloc {
        override fun onStartCookingClicked() = Unit
    }

@Preview
@Composable
internal fun WelcomeScreenPreview() {
    ChefMateTheme { WelcomeScreen(bloc = previewWelcomeBloc) }
}

@Preview
@Composable
internal fun SaveRecipesScreenPreview() {
    ChefMateTheme { SaveRecipesScreen(bloc = previewSaveRecipesBloc) }
}

@Preview
@Composable
internal fun CookModeScreenPreview() {
    ChefMateTheme { CookModeScreen(bloc = previewCookModeBloc) }
}

@Preview
@Composable
internal fun GroceryListScreenPreview() {
    ChefMateTheme { GroceryListScreen(bloc = previewGroceryListBloc) }
}

@Preview
@Composable
internal fun MealPlanningScreenPreview() {
    ChefMateTheme { MealPlanningScreen(bloc = previewMealPlanningBloc) }
}

@Preview
@Composable
internal fun StartCookingScreenPreview() {
    ChefMateTheme { StartCookingScreen(bloc = previewStartCookingBloc) }
}
