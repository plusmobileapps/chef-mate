package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListScreen
import com.plusmobileapps.chefmate.recipe.core.addgrocery.previewAddToGroceryBloc
import com.plusmobileapps.chefmate.recipe.core.addgrocery.previewAddToGroceryBlocEmpty
import com.plusmobileapps.chefmate.recipe.core.addgrocery.previewAddToGroceryBlocScaled
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun AddRecipeToGroceryListScreenshot() {
    ChefMateTheme { AddRecipeToGroceryListScreen(bloc = previewAddToGroceryBloc) }
}

// A recipe the user has already scaled: the control reads 2× and the listed amounts are doubled.
@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun AddRecipeToGroceryListScaledScreenshot() {
    ChefMateTheme { AddRecipeToGroceryListScreen(bloc = previewAddToGroceryBlocScaled) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 700, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddRecipeToGroceryListScaledDarkScreenshot() {
    ChefMateTheme(darkTheme = true) {
        AddRecipeToGroceryListScreen(bloc = previewAddToGroceryBlocScaled)
    }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun AddRecipeToGroceryListEmptyScreenshot() {
    ChefMateTheme { AddRecipeToGroceryListScreen(bloc = previewAddToGroceryBlocEmpty) }
}
