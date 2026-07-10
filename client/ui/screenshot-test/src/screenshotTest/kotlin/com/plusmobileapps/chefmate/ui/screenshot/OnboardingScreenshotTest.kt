package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.onboarding.OnboardingNavBar
import com.plusmobileapps.chefmate.onboarding.impl.ui.previewCookModeBloc
import com.plusmobileapps.chefmate.onboarding.impl.ui.previewGroceryListBloc
import com.plusmobileapps.chefmate.onboarding.impl.ui.previewMealPlanningBloc
import com.plusmobileapps.chefmate.onboarding.impl.ui.previewSaveRecipesBloc
import com.plusmobileapps.chefmate.onboarding.impl.ui.previewStartCookingBloc
import com.plusmobileapps.chefmate.onboarding.impl.ui.previewStartCookingSignedInBloc
import com.plusmobileapps.chefmate.onboarding.impl.ui.previewWelcomeBloc
import com.plusmobileapps.chefmate.onboarding.impl.ui.previewWelcomeSignedInBloc
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
private fun OnboardingScreenshot(content: @Composable () -> Unit, darkTheme: Boolean = false) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

// ── Nav bar ────────────────────────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true)
@Composable
fun OnboardingNavBarLightScreenshot() {
    OnboardingScreenshot(
        content = {
            OnboardingNavBar(currentStep = 1, totalSteps = 6, onBackClick = {}, onSkipClick = {})
        }
    )
}

@PreviewTest
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OnboardingNavBarDarkScreenshot() {
    OnboardingScreenshot(
        content = {
            OnboardingNavBar(currentStep = 1, totalSteps = 6, onBackClick = {}, onSkipClick = {})
        },
        darkTheme = true,
    )
}

// ── Welcome ────────────────────────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OnboardingWelcomeLightScreenshot() {
    OnboardingScreenshot(content = { previewWelcomeBloc.Content() })
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OnboardingWelcomeDarkScreenshot() {
    OnboardingScreenshot(content = { previewWelcomeBloc.Content() }, darkTheme = true)
}

// Re-entry variant for an already signed-in user — the sign-in button is hidden.

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OnboardingWelcomeSignedInLightScreenshot() {
    OnboardingScreenshot(content = { previewWelcomeSignedInBloc.Content() })
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OnboardingWelcomeSignedInDarkScreenshot() {
    OnboardingScreenshot(content = { previewWelcomeSignedInBloc.Content() }, darkTheme = true)
}

// ── Save recipes ─────────────────────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OnboardingSaveRecipesLightScreenshot() {
    OnboardingScreenshot(content = { previewSaveRecipesBloc.Content() })
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OnboardingSaveRecipesDarkScreenshot() {
    OnboardingScreenshot(content = { previewSaveRecipesBloc.Content() }, darkTheme = true)
}

// ── Cook mode ────────────────────────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OnboardingCookModeLightScreenshot() {
    OnboardingScreenshot(content = { previewCookModeBloc.Content() })
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OnboardingCookModeDarkScreenshot() {
    OnboardingScreenshot(content = { previewCookModeBloc.Content() }, darkTheme = true)
}

// ── Grocery list ─────────────────────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OnboardingGroceryListLightScreenshot() {
    OnboardingScreenshot(content = { previewGroceryListBloc.Content() })
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OnboardingGroceryListDarkScreenshot() {
    OnboardingScreenshot(content = { previewGroceryListBloc.Content() }, darkTheme = true)
}

// ── Meal planning ────────────────────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OnboardingMealPlanningLightScreenshot() {
    OnboardingScreenshot(content = { previewMealPlanningBloc.Content() })
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OnboardingMealPlanningDarkScreenshot() {
    OnboardingScreenshot(content = { previewMealPlanningBloc.Content() }, darkTheme = true)
}

// ── Start cooking ────────────────────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OnboardingStartCookingLightScreenshot() {
    OnboardingScreenshot(content = { previewStartCookingBloc.Content() })
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OnboardingStartCookingDarkScreenshot() {
    OnboardingScreenshot(content = { previewStartCookingBloc.Content() }, darkTheme = true)
}

// Re-entry variant for an already signed-in user — the sign-up button is hidden.

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun OnboardingStartCookingSignedInLightScreenshot() {
    OnboardingScreenshot(content = { previewStartCookingSignedInBloc.Content() })
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OnboardingStartCookingSignedInDarkScreenshot() {
    OnboardingScreenshot(content = { previewStartCookingSignedInBloc.Content() }, darkTheme = true)
}
