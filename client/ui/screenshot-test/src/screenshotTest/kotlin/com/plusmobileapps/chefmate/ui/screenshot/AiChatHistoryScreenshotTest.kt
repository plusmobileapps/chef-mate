package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.aichat.AiChatHistoryBloc
import com.plusmobileapps.chefmate.aichat.impl.ui.previewAiChatHistoryBloc
import com.plusmobileapps.chefmate.aichat.impl.ui.previewAiChatHistoryBlocEmpty
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
private fun AiChatHistoryScreenshot(bloc: AiChatHistoryBloc, darkTheme: Boolean = false) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            bloc.Content()
        }
    }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun AiChatHistoryListLightScreenshot() {
    AiChatHistoryScreenshot(bloc = previewAiChatHistoryBloc)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AiChatHistoryListDarkScreenshot() {
    AiChatHistoryScreenshot(bloc = previewAiChatHistoryBloc, darkTheme = true)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun AiChatHistoryEmptyLightScreenshot() {
    AiChatHistoryScreenshot(bloc = previewAiChatHistoryBlocEmpty)
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AiChatHistoryEmptyDarkScreenshot() {
    AiChatHistoryScreenshot(bloc = previewAiChatHistoryBlocEmpty, darkTheme = true)
}
