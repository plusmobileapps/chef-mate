package com.plusmobileapps.chefmate.onboarding.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.onboarding.CookModeBloc
import com.plusmobileapps.chefmate.onboarding.GroceryListBloc
import com.plusmobileapps.chefmate.onboarding.MealPlanningBloc
import com.plusmobileapps.chefmate.onboarding.SaveRecipesBloc
import com.plusmobileapps.chefmate.onboarding.StartCookingBloc
import com.plusmobileapps.chefmate.onboarding.WelcomeBloc
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// Public fake Blocs so the screenshot-test module can reuse them. The onboarding step screens are
// stateless, so the fakes only need to satisfy the interface.

val previewWelcomeBloc: WelcomeBloc =
    object : WelcomeBloc {
        override fun onGetStartedClicked() = Unit

        override fun onSignInClicked() = Unit

        override fun onSkipClicked() = Unit

        @Composable override fun Content(modifier: Modifier) = WelcomeScreen(this, modifier)
    }

val previewSaveRecipesBloc: SaveRecipesBloc =
    object : SaveRecipesBloc {
        override fun onNextClicked() = Unit

        @Composable override fun Content(modifier: Modifier) = SaveRecipesScreen(this, modifier)
    }

val previewCookModeBloc: CookModeBloc =
    object : CookModeBloc {
        override fun onNextClicked() = Unit

        @Composable override fun Content(modifier: Modifier) = CookModeScreen(this, modifier)
    }

val previewGroceryListBloc: GroceryListBloc =
    object : GroceryListBloc {
        override fun onNextClicked() = Unit

        @Composable override fun Content(modifier: Modifier) = GroceryListScreen(this, modifier)
    }

val previewMealPlanningBloc: MealPlanningBloc =
    object : MealPlanningBloc {
        override fun onNextClicked() = Unit

        @Composable override fun Content(modifier: Modifier) = MealPlanningScreen(this, modifier)
    }

val previewStartCookingBloc: StartCookingBloc =
    object : StartCookingBloc {
        override fun onStartCookingClicked() = Unit

        @Composable override fun Content(modifier: Modifier) = StartCookingScreen(this, modifier)
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
