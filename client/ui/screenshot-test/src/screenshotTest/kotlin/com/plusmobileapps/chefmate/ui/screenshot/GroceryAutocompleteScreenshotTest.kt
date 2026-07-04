package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteSettingsScreen
import com.plusmobileapps.chefmate.grocery.autocomplete.previewGroceryAutocompleteBloc
import com.plusmobileapps.chefmate.grocery.autocomplete.previewGroceryAutocompleteBlocCreating
import com.plusmobileapps.chefmate.grocery.autocomplete.previewGroceryAutocompleteBlocEmptyUser
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun GroceryAutocompleteScreenshot() {
    ChefMateTheme { GroceryAutocompleteSettingsScreen(bloc = previewGroceryAutocompleteBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GroceryAutocompleteDarkScreenshot() {
    ChefMateTheme(darkTheme = true) {
        GroceryAutocompleteSettingsScreen(bloc = previewGroceryAutocompleteBloc)
    }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun GroceryAutocompleteEmptyUserScreenshot() {
    ChefMateTheme {
        GroceryAutocompleteSettingsScreen(bloc = previewGroceryAutocompleteBlocEmptyUser)
    }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun GroceryAutocompleteCreatingScreenshot() {
    ChefMateTheme {
        GroceryAutocompleteSettingsScreen(bloc = previewGroceryAutocompleteBlocCreating)
    }
}
