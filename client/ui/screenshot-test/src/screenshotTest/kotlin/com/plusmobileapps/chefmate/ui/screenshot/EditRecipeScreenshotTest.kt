package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.recipe.core.impl.edit.ui.previewEditRecipeBloc
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// Snapshot coverage for the recipe edit form. The form is long, so we render on a tall canvas to
// capture the lower fields (numeric inputs + the resizable ingredients/directions fields with their
// drag handles) where the polish from this PR lives.
@Composable
private fun EditRecipeScreenshot(darkTheme: Boolean = false) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            previewEditRecipeBloc.Content()
        }
    }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 2400)
@Composable
fun EditRecipeLightScreenshot() {
    EditRecipeScreenshot()
}

@PreviewTest
@Preview(showBackground = true, heightDp = 2400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditRecipeDarkScreenshot() {
    EditRecipeScreenshot(darkTheme = true)
}

// Wide (expanded) window: the form spreads into the photo + metadata top row and the
// ingredients | description+directions two-pane split.
@PreviewTest
@Preview(showBackground = true, widthDp = 1280, heightDp = 1300)
@Composable
fun EditRecipeWideLightScreenshot() {
    EditRecipeScreenshot()
}

@PreviewTest
@Preview(
    showBackground = true,
    widthDp = 1280,
    heightDp = 1300,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun EditRecipeWideDarkScreenshot() {
    EditRecipeScreenshot(darkTheme = true)
}
