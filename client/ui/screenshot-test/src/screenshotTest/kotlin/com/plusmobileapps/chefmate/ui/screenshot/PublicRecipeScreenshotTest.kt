package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.recipe.core.share.PublicRecipePreviewScreen
import com.plusmobileapps.chefmate.recipe.core.share.previewPublicRecipeLoadedBloc
import com.plusmobileapps.chefmate.recipe.core.share.previewPublicRecipeNotFoundBloc
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun PublicRecipeLoadedScreenshot() {
    ChefMateTheme { PublicRecipePreviewScreen(bloc = previewPublicRecipeLoadedBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PublicRecipeLoadedDarkScreenshot() {
    ChefMateTheme(darkTheme = true) {
        PublicRecipePreviewScreen(bloc = previewPublicRecipeLoadedBloc)
    }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 600)
@Composable
fun PublicRecipeNotFoundScreenshot() {
    ChefMateTheme { PublicRecipePreviewScreen(bloc = previewPublicRecipeNotFoundBloc) }
}
