package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBarPreview
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true)
@Composable
fun BottomNavBarNoBadgeScreenshot() {
    ChefMateTheme { Surface { BottomNavBarPreview(notificationCount = 0) } }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun BottomNavBarWithBadgeScreenshot() {
    ChefMateTheme { Surface { BottomNavBarPreview(notificationCount = 3) } }
}

@PreviewTest
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BottomNavBarWithBadgeDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { Surface { BottomNavBarPreview(notificationCount = 3) } }
}
