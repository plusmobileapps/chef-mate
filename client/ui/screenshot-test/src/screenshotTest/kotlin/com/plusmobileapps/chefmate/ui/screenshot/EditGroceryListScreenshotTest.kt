package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.grocery.core.edit.EditGroceryListBloc
import com.plusmobileapps.chefmate.grocery.core.impl.edit.ui.previewEditGroceryListBlocAuthenticated
import com.plusmobileapps.chefmate.grocery.core.impl.edit.ui.previewEditGroceryListBlocDeleteConfirm
import com.plusmobileapps.chefmate.grocery.core.impl.edit.ui.previewEditGroceryListBlocUnauthenticated
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
private fun EditGroceryListScreenshot(bloc: EditGroceryListBloc, darkTheme: Boolean = false) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            bloc.Content()
        }
    }
}

// ── Authenticated owner managing collaborators ─────────────────────────────

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun EditGroceryListAuthenticatedLightScreenshot() {
    EditGroceryListScreenshot(bloc = previewEditGroceryListBlocAuthenticated)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditGroceryListAuthenticatedDarkScreenshot() {
    EditGroceryListScreenshot(bloc = previewEditGroceryListBlocAuthenticated, darkTheme = true)
}

// ── Signed-out state: sign in / sign up ────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun EditGroceryListUnauthenticatedLightScreenshot() {
    EditGroceryListScreenshot(bloc = previewEditGroceryListBlocUnauthenticated)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditGroceryListUnauthenticatedDarkScreenshot() {
    EditGroceryListScreenshot(bloc = previewEditGroceryListBlocUnauthenticated, darkTheme = true)
}

// ── Delete confirmation dialog ─────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun EditGroceryListDeleteConfirmScreenshot() {
    EditGroceryListScreenshot(bloc = previewEditGroceryListBlocDeleteConfirm)
}
