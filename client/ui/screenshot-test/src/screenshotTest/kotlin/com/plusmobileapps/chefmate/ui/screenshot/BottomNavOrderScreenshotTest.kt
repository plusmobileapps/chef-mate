package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderScreen
import com.plusmobileapps.chefmate.recipe.bottomnav.previewBottomNavOrderBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.previewBottomNavOrderDirtyBloc
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true)
@Composable
fun BottomNavOrderLightScreenshot() {
    ChefMateTheme { BottomNavOrderScreen(bloc = previewBottomNavOrderBloc) }
}

@PreviewTest
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BottomNavOrderDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { BottomNavOrderScreen(bloc = previewBottomNavOrderBloc) }
}

// Locks in the dirty-state visual: a non-default order with the Save button rendered enabled.
@PreviewTest
@Preview(showBackground = true)
@Composable
fun BottomNavOrderReorderedDirtyScreenshot() {
    ChefMateTheme { BottomNavOrderScreen(bloc = previewBottomNavOrderDirtyBloc) }
}
