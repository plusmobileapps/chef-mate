package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.aichat.AiChatBloc
import com.plusmobileapps.chefmate.aichat.impl.ui.previewAiChatBloc
import com.plusmobileapps.chefmate.aichat.impl.ui.previewAiChatBlocEmpty
import com.plusmobileapps.chefmate.aichat.impl.ui.previewAiChatBlocError
import com.plusmobileapps.chefmate.aichat.impl.ui.previewAiChatBlocExtracting
import com.plusmobileapps.chefmate.aichat.impl.ui.previewAiChatBlocStreaming
import com.plusmobileapps.chefmate.aichat.impl.ui.previewAiChatBlocThinking
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
private fun AiChatScreenshot(bloc: AiChatBloc, darkTheme: Boolean = false) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            bloc.Content()
        }
    }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun AiChatEmptyLightScreenshot() {
    AiChatScreenshot(bloc = previewAiChatBlocEmpty)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AiChatEmptyDarkScreenshot() {
    AiChatScreenshot(bloc = previewAiChatBlocEmpty, darkTheme = true)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun AiChatConversationLightScreenshot() {
    AiChatScreenshot(bloc = previewAiChatBloc)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AiChatConversationDarkScreenshot() {
    AiChatScreenshot(bloc = previewAiChatBloc, darkTheme = true)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun AiChatStreamingLightScreenshot() {
    AiChatScreenshot(bloc = previewAiChatBlocStreaming)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun AiChatThinkingLightScreenshot() {
    AiChatScreenshot(bloc = previewAiChatBlocThinking)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun AiChatErrorLightScreenshot() {
    AiChatScreenshot(bloc = previewAiChatBlocError)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun AiChatExtractingPillLightScreenshot() {
    AiChatScreenshot(bloc = previewAiChatBlocExtracting)
}
