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

@Composable
private fun EditorPreview(richTextMode: Boolean, darkTheme: Boolean = false) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlusMarkdownEditor(
                value = SAMPLE,
                onValueChange = {},
                label = "Directions",
                placeholder = "Enter cooking directions",
                richTextMode = richTextMode,
                onRichTextModeChange = {},
                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
