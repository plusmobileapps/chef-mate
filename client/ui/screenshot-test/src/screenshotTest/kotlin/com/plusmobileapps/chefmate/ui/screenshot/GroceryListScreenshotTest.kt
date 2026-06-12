package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.grocery.core.impl.list.ui.GroceryListScreen
import com.plusmobileapps.chefmate.grocery.core.impl.list.ui.previewGroceryListBloc
import com.plusmobileapps.chefmate.grocery.core.impl.list.ui.previewGroceryListBlocAutocomplete
import com.plusmobileapps.chefmate.grocery.core.impl.list.ui.previewGroceryListBlocEmpty
import com.plusmobileapps.chefmate.grocery.core.impl.list.ui.previewGroceryListBlocFilteredEmpty
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// GroceryListScreen has no top-level Surface — its background comes from the app shell in
// production. Wrap each test in one so dark snapshots actually render dark.
@Composable
private fun GroceryListScreenshot(bloc: GroceryListBloc, darkTheme: Boolean = false) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            bloc.Content()
        }
    }
}

@Composable
private fun GroceryListAutocompleteScreenshot(darkTheme: Boolean = false) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            GroceryListScreen(
                bloc = previewGroceryListBlocAutocomplete,
                forceShowAutocompleteSuggestions = true,
            )
        }
    }
}

// ── Populated list ─────────────────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun GroceryListPhonePortraitLightScreenshot() {
    GroceryListScreenshot(bloc = previewGroceryListBloc)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GroceryListPhonePortraitDarkScreenshot() {
    GroceryListScreenshot(bloc = previewGroceryListBloc, darkTheme = true)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun GroceryListAutocompletePhonePortraitLightScreenshot() {
    GroceryListAutocompleteScreenshot()
}

// ── Empty state — regression coverage for the "Browse my recipes" CTA ──────

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun GroceryListEmptyPhonePortraitLightScreenshot() {
    GroceryListScreenshot(bloc = previewGroceryListBlocEmpty)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GroceryListEmptyPhonePortraitDarkScreenshot() {
    GroceryListScreenshot(bloc = previewGroceryListBlocEmpty, darkTheme = true)
}

// Landscape (short viewport): the empty state must stay vertically centered and become
// scrollable rather than getting cut off — regression coverage for #240.
@PreviewTest
@Preview(showBackground = true, widthDp = 740, heightDp = 360)
@Composable
fun GroceryListEmptyPhoneLandscapeLightScreenshot() {
    GroceryListScreenshot(bloc = previewGroceryListBlocEmpty)
}

// ── Filtered empty state — "Clear filters" + "Browse my recipes" recovery CTAs ──

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun GroceryListFilteredEmptyPhonePortraitLightScreenshot() {
    GroceryListScreenshot(bloc = previewGroceryListBlocFilteredEmpty)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GroceryListFilteredEmptyPhonePortraitDarkScreenshot() {
    GroceryListScreenshot(bloc = previewGroceryListBlocFilteredEmpty, darkTheme = true)
}

// Landscape (short viewport): the filtered empty state must stay centered and scroll rather than
// getting cut off — regression coverage for #240.
@PreviewTest
@Preview(showBackground = true, widthDp = 740, heightDp = 360)
@Composable
fun GroceryListFilteredEmptyPhoneLandscapeLightScreenshot() {
    GroceryListScreenshot(bloc = previewGroceryListBlocFilteredEmpty)
}
