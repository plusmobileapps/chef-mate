package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.ui.text.toInlineMarkdownAnnotatedString
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// Renders sample ingredient/direction lines through the same inline-markdown parser the recipe
// detail screen uses, so the snapshot shows exactly how stored `**bold**` / `_italic_` markers
// display once a recipe is saved.
private val sampleLines =
    listOf(
        "400g spaghetti (plain line)",
        "**400g** spaghetti — bold quantity",
        "Cook until _al dente_ — italic note",
        "**Sauce:** stir in _gently_ — bold and italic",
        "A stray * or _ stays literal",
    )

@Composable
private fun InlineMarkdownSample(darkTheme: Boolean = false) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sampleLines.forEach { line ->
                    Text(
                        text = line.toInlineMarkdownAnnotatedString(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun InlineMarkdownLightScreenshot() {
    InlineMarkdownSample()
}

@PreviewTest
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InlineMarkdownDarkScreenshot() {
    InlineMarkdownSample(darkTheme = true)
}
