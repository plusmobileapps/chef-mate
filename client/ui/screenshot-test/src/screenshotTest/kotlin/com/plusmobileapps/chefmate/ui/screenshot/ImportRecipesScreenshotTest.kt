package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.recipe.importer.previewImportRecipesDoneBloc
import com.plusmobileapps.chefmate.recipe.importer.previewImportRecipesEmptyBloc
import com.plusmobileapps.chefmate.recipe.importer.previewImportRecipesErrorBloc
import com.plusmobileapps.chefmate.recipe.importer.previewImportRecipesImportingBloc
import com.plusmobileapps.chefmate.recipe.importer.previewImportRecipesReviewBloc
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ImportRecipesEmptyScreenshot() {
    ChefMateTheme { previewImportRecipesEmptyBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ImportRecipesReviewScreenshot() {
    ChefMateTheme { previewImportRecipesReviewBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ImportRecipesReviewDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewImportRecipesReviewBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ImportRecipesImportingScreenshot() {
    ChefMateTheme { previewImportRecipesImportingBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ImportRecipesDoneScreenshot() {
    ChefMateTheme { previewImportRecipesDoneBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ImportRecipesErrorScreenshot() {
    ChefMateTheme { previewImportRecipesErrorBloc.Content() }
}
