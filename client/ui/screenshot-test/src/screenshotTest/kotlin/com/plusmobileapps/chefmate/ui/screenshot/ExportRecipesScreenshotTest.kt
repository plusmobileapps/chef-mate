package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.recipe.exporter.previewExportRecipesDoneBloc
import com.plusmobileapps.chefmate.recipe.exporter.previewExportRecipesEmptyBloc
import com.plusmobileapps.chefmate.recipe.exporter.previewExportRecipesErrorBloc
import com.plusmobileapps.chefmate.recipe.exporter.previewExportRecipesExportingBloc
import com.plusmobileapps.chefmate.recipe.exporter.previewExportRecipesLoadingBloc
import com.plusmobileapps.chefmate.recipe.exporter.previewExportRecipesReviewBloc
import com.plusmobileapps.chefmate.recipe.exporter.previewExportRecipesReviewNoSelectionBloc
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ExportRecipesLoadingScreenshot() {
    ChefMateTheme { previewExportRecipesLoadingBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ExportRecipesEmptyScreenshot() {
    ChefMateTheme { previewExportRecipesEmptyBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ExportRecipesReviewScreenshot() {
    ChefMateTheme { previewExportRecipesReviewBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ExportRecipesReviewNoSelectionScreenshot() {
    // FAB is gated on "at least one selected" — this snapshot locks in that the action chip is
    // absent when nothing is picked, complementing the standard review shot above.
    ChefMateTheme { previewExportRecipesReviewNoSelectionBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ExportRecipesReviewDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewExportRecipesReviewBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ExportRecipesExportingScreenshot() {
    ChefMateTheme { previewExportRecipesExportingBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ExportRecipesDoneScreenshot() {
    ChefMateTheme { previewExportRecipesDoneBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ExportRecipesErrorScreenshot() {
    ChefMateTheme { previewExportRecipesErrorBloc.Content() }
}
