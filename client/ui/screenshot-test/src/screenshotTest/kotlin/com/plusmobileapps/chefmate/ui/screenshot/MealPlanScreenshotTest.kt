package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.meal.core.impl.ui.previewMealPlanBloc
import com.plusmobileapps.chefmate.meal.core.impl.ui.previewMealPlanBlocCooking
import com.plusmobileapps.chefmate.meal.core.impl.ui.previewMealPlanBlocEmpty
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun MealPlanLightScreenshot() {
    ChefMateTheme { previewMealPlanBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MealPlanDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewMealPlanBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun MealPlanCookingSessionScreenshot() {
    ChefMateTheme { previewMealPlanBlocCooking.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun MealPlanEmptyScreenshot() {
    ChefMateTheme { previewMealPlanBlocEmpty.Content() }
}
