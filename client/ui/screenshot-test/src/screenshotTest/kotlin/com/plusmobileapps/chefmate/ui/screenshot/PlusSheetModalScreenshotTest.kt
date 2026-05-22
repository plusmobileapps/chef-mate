package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.components.PlusSheetModalHeader
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// Snapshot coverage for PlusSheetModal — we render the header directly because ModalBottomSheet
// doesn't render reliably under the Compose screenshot plugin (same workaround used elsewhere).

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PlusSheetModalHeaderWithTitleAndCloseScreenshot() {
    ChefMateTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            PlusSheetModalHeader(title = FixedString("Edit item"), onCloseClick = {})
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PlusSheetModalHeaderCloseOnlyScreenshot() {
    ChefMateTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            PlusSheetModalHeader(title = null, onCloseClick = {})
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PlusSheetModalHeaderTitleOnlyScreenshot() {
    ChefMateTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            PlusSheetModalHeader(title = FixedString("Title only"), onCloseClick = null)
        }
    }
}

@PreviewTest
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PlusSheetModalHeaderDarkScreenshot() {
    ChefMateTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            PlusSheetModalHeader(title = FixedString("Edit item"), onCloseClick = {})
        }
    }
}
