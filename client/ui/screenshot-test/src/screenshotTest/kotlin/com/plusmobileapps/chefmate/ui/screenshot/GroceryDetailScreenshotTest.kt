package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailSheetContent
import com.plusmobileapps.chefmate.grocery.core.detail.previewGroceryDetailBlocFromRecipe
import com.plusmobileapps.chefmate.grocery.core.detail.previewGroceryDetailBlocLoaded
import com.plusmobileapps.chefmate.grocery.core.detail.previewGroceryDetailBlocLoading
import com.plusmobileapps.chefmate.grocery.core.detail.previewGroceryDetailBlocPurchased
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// ModalBottomSheet doesn't render reliably under the Compose screenshot test plugin, so these
// tests target [GroceryDetailSheetContent] directly. The list-screen overlay is exercised at
// runtime via robot UI tests in client/composeApp/src/commonTest.
@Composable
private fun GroceryDetailSheet(bloc: GroceryDetailBloc, darkTheme: Boolean = false) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
            GroceryDetailSheetContent(bloc = bloc)
        }
    }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 500)
@Composable
fun GroceryDetailSheetLoadedLightScreenshot() {
    GroceryDetailSheet(bloc = previewGroceryDetailBlocLoaded)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 500, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GroceryDetailSheetLoadedDarkScreenshot() {
    GroceryDetailSheet(bloc = previewGroceryDetailBlocLoaded, darkTheme = true)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 500)
@Composable
fun GroceryDetailSheetPurchasedLightScreenshot() {
    GroceryDetailSheet(bloc = previewGroceryDetailBlocPurchased)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 500)
@Composable
fun GroceryDetailSheetFromRecipeLightScreenshot() {
    GroceryDetailSheet(bloc = previewGroceryDetailBlocFromRecipe)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 300)
@Composable
fun GroceryDetailSheetLoadingLightScreenshot() {
    GroceryDetailSheet(bloc = previewGroceryDetailBlocLoading)
}
