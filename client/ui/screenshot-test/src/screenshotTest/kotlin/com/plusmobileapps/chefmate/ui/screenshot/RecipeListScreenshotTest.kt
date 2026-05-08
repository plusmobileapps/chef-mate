package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc
import com.plusmobileapps.chefmate.recipe.list.RecipeListScreen
import com.plusmobileapps.chefmate.recipe.list.previewRecipeListBloc
import com.plusmobileapps.chefmate.recipe.list.previewRecipeListBlocCooking
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// RecipeListScreen has no top-level Surface — in production its background comes from the app
// shell (Scaffold / PlusNavRailHeaderContainer). Wrap each test in one so dark snapshots actually
// render dark instead of inheriting the default white screenshot canvas.
@Composable
private fun RecipeListScreenshot(bloc: RecipeListBloc, darkTheme: Boolean = false) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            RecipeListScreen(bloc = bloc)
        }
    }
}

// ── Phone portrait (360 × 1100 dp) ─────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun RecipeListPhonePortraitLightScreenshot() {
    RecipeListScreenshot(bloc = previewRecipeListBloc)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RecipeListPhonePortraitDarkScreenshot() {
    RecipeListScreenshot(bloc = previewRecipeListBloc, darkTheme = true)
}

// ── Cooking-session FAB stack — regression coverage for #150 ───────────────
// The Continue/Done Cooking FAB stack used to double-apply navigationBars +
// displayCutout insets and render off the bottom edge of its container.

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun RecipeListCookingPhonePortraitLightScreenshot() {
    RecipeListScreenshot(bloc = previewRecipeListBlocCooking)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RecipeListCookingPhonePortraitDarkScreenshot() {
    RecipeListScreenshot(bloc = previewRecipeListBlocCooking, darkTheme = true)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 580, heightDp = 360)
@Composable
fun RecipeListCookingPhoneLandscapeScreenshot() {
    RecipeListScreenshot(bloc = previewRecipeListBlocCooking)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 800, heightDp = 1100)
@Composable
fun RecipeListCookingTabletScreenshot() {
    RecipeListScreenshot(bloc = previewRecipeListBlocCooking)
}
