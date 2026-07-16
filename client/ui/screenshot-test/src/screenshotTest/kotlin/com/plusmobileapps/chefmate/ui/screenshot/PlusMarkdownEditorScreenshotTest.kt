package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.ui.components.PlusMarkdownEditor
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

private const val SAMPLE = "Cook the **pasta** until _al dente_, then drain."

// A multi-line numbered sample that also exercises the bulleted/numbered list toolbar buttons.
private const val LIST_SAMPLE =
    "1. Boil the **water**.\n2. Add _pasta_ and cook until al dente.\n3. Drain and serve."

@Composable
private fun EditorPreview(
    richTextMode: Boolean,
    darkTheme: Boolean = false,
    showListButtons: Boolean = false,
    value: String = SAMPLE,
) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlusMarkdownEditor(
                value = value,
                onValueChange = {},
                label = "Directions",
                placeholder = "Enter cooking directions",
                richTextMode = richTextMode,
                onRichTextModeChange = {},
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                showListButtons = showListButtons,
            )
        }
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 420)
@Composable
fun PlusMarkdownEditorMarkdownLightScreenshot() {
    EditorPreview(richTextMode = false)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 420, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PlusMarkdownEditorMarkdownDarkScreenshot() {
    EditorPreview(richTextMode = false, darkTheme = true)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 420)
@Composable
fun PlusMarkdownEditorRichTextLightScreenshot() {
    EditorPreview(richTextMode = true)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 420, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PlusMarkdownEditorRichTextDarkScreenshot() {
    EditorPreview(richTextMode = true, darkTheme = true)
}

// The one-item-per-line fields (ingredients/directions) surface bulleted + numbered list buttons
// alongside bold/italic.
@PreviewTest
@Preview(showBackground = true, widthDp = 420)
@Composable
fun PlusMarkdownEditorListButtonsLightScreenshot() {
    EditorPreview(richTextMode = false, showListButtons = true, value = LIST_SAMPLE)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 420, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PlusMarkdownEditorListButtonsDarkScreenshot() {
    EditorPreview(
        richTextMode = false,
        darkTheme = true,
        showListButtons = true,
        value = LIST_SAMPLE,
    )
}
