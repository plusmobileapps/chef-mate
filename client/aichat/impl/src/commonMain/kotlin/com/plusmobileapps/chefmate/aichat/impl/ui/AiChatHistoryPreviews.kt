package com.plusmobileapps.chefmate.aichat.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.aichat.AiChatConversation
import com.plusmobileapps.chefmate.aichat.AiChatHistoryBloc
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow

private fun historyBloc(model: AiChatHistoryBloc.Model): AiChatHistoryBloc =
    object : AiChatHistoryBloc {
        override val state = MutableStateFlow(model)

        override fun onConversationClick(conversationId: Long) = Unit

        override fun onDeleteConversationClick(conversationId: Long) = Unit

        override fun onDeleteAllClick() = Unit

        override fun onNewConversationClick() = Unit

        override fun onBackClicked() = Unit

        @Composable
        override fun Content(modifier: Modifier) =
            AiChatHistoryScreen(bloc = this, modifier = modifier)
    }

private val sampleConversations =
    persistentListOf(
        AiChatConversation(
            id = 1L,
            title = "Quick weeknight chicken dinner ideas under 30 minutes",
            createdAt = 1_700_000_000_000,
            updatedAt = 1_700_100_000_000,
        ),
        AiChatConversation(
            id = 2L,
            title = "What can I cook with chicken thighs and lemon?",
            createdAt = 1_700_000_000_000,
            updatedAt = 1_700_050_000_000,
        ),
        AiChatConversation(
            id = 3L,
            title = null,
            createdAt = 1_700_000_000_000,
            updatedAt = 1_700_010_000_000,
        ),
    )

val previewAiChatHistoryBlocEmpty: AiChatHistoryBloc = historyBloc(AiChatHistoryBloc.Model())

val previewAiChatHistoryBloc: AiChatHistoryBloc =
    historyBloc(AiChatHistoryBloc.Model(conversations = sampleConversations))

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun AiChatHistoryScreenPreview() {
    ChefMateTheme { previewAiChatHistoryBloc.Content() }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun AiChatHistoryScreenEmptyPreview() {
    ChefMateTheme { previewAiChatHistoryBlocEmpty.Content() }
}
