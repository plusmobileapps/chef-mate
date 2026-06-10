package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.ui.components.PlusOutlinedContainer
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
private fun ContainerPreview(expanded: Boolean, darkTheme: Boolean = false) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlusOutlinedContainer(
                title = "More details",
                initiallyExpanded = expanded,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                OutlinedTextField(
                    value = "https://example.com/recipe",
                    onValueChange = {},
                    label = { Text("Source URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = "4",
                    onValueChange = {},
                    label = { Text("Servings") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 420)
@Composable
fun PlusOutlinedContainerCollapsedScreenshot() {
    ContainerPreview(expanded = false)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 420)
@Composable
fun PlusOutlinedContainerExpandedScreenshot() {
    ContainerPreview(expanded = true)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 420, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PlusOutlinedContainerExpandedDarkScreenshot() {
    ContainerPreview(expanded = true, darkTheme = true)
}
