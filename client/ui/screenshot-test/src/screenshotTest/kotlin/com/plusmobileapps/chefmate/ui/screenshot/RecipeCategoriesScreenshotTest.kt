package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.recipe.categories.impl.ui.RecipeCategoriesScreen
import com.plusmobileapps.chefmate.recipe.categories.previewRecipeCategoriesBloc
import com.plusmobileapps.chefmate.recipe.categories.previewRecipeCategoriesBlocBulkDeleteDialog
import com.plusmobileapps.chefmate.recipe.categories.previewRecipeCategoriesBlocCreating
import com.plusmobileapps.chefmate.recipe.categories.previewRecipeCategoriesBlocEmptyUser
import com.plusmobileapps.chefmate.recipe.categories.previewRecipeCategoriesBlocSelectionMode
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun RecipeCategoriesScreenshot() {
    ChefMateTheme { RecipeCategoriesScreen(bloc = previewRecipeCategoriesBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RecipeCategoriesDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { RecipeCategoriesScreen(bloc = previewRecipeCategoriesBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun RecipeCategoriesSelectionModeScreenshot() {
    ChefMateTheme { RecipeCategoriesScreen(bloc = previewRecipeCategoriesBlocSelectionMode) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun RecipeCategoriesCreatingScreenshot() {
    ChefMateTheme { RecipeCategoriesScreen(bloc = previewRecipeCategoriesBlocCreating) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun RecipeCategoriesEmptyUserScreenshot() {
    ChefMateTheme { RecipeCategoriesScreen(bloc = previewRecipeCategoriesBlocEmptyUser) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun RecipeCategoriesBulkDeleteDialogScreenshot() {
    ChefMateTheme { RecipeCategoriesScreen(bloc = previewRecipeCategoriesBlocBulkDeleteDialog) }
}
