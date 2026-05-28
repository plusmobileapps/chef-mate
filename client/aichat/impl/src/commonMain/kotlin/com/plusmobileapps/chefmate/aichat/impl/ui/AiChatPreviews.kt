package com.plusmobileapps.chefmate.aichat.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.aichat.AiChatBloc
import com.plusmobileapps.chefmate.aichat.ChatMessage
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.MutableStateFlow

private fun aiChatBloc(model: AiChatBloc.Model, input: String = ""): AiChatBloc =
    object : AiChatBloc {
        override val state = MutableStateFlow(model)
        override val inputText = MutableStateFlow(input)

        override fun onInputChange(text: String) = Unit

        override fun onSendClick() = Unit

        override fun onClearClick() = Unit

        override fun onAddRecipeClick() = Unit

        override fun onBackClicked() = Unit

        @Composable
        override fun Content(modifier: Modifier) = AiChatScreen(bloc = this, modifier = modifier)
    }

private val sampleConversation =
    listOf(
        ChatMessage(
            id = 1L,
            role = ChatMessage.Role.USER,
            content = "What can I cook with chicken thighs and lemon?",
            isStreaming = false,
        ),
        ChatMessage(
            id = 2L,
            role = ChatMessage.Role.MODEL,
            content =
                """
                Here are a few options:

                - **Lemon-roasted thighs** — sear skin-side down, then roast with garlic, thyme, and
                  lemon slices at 425°F for 25 minutes.
                - **Greek-style sheet pan** — toss thighs with olive oil, oregano, lemon juice, and
                  potatoes; roast 35 minutes.

                Let me know how much time you have and I can narrow it down.
                """
                    .trimIndent(),
            isStreaming = false,
        ),
        ChatMessage(
            id = 3L,
            role = ChatMessage.Role.USER,
            content = "Anything quicker for a weeknight?",
            isStreaming = false,
        ),
    )

/** Empty conversation — used to verify the welcome state renders. */
val previewAiChatBlocEmpty: AiChatBloc = aiChatBloc(AiChatBloc.Model())

/** Populated conversation — alternating user / model bubbles. Shows the Add Recipe pill. */
val previewAiChatBloc: AiChatBloc =
    aiChatBloc(AiChatBloc.Model(messages = sampleConversation, canAddRecipe = true))

/** Same as [previewAiChatBloc] but with the pill in its loading state. */
val previewAiChatBlocExtracting: AiChatBloc =
    aiChatBloc(
        AiChatBloc.Model(
            messages = sampleConversation,
            canAddRecipe = true,
            isExtractingRecipe = true,
        )
    )

/** Streaming response — last model bubble is mid-stream and shows the progress indicator. */
val previewAiChatBlocStreaming: AiChatBloc =
    aiChatBloc(
        AiChatBloc.Model(
            messages =
                sampleConversation +
                    ChatMessage(
                        id = 4L,
                        role = ChatMessage.Role.MODEL,
                        content = "Sure! A 15-minute option is pan-seared chicken thighs with",
                        isStreaming = true,
                    ),
            isSending = true,
        )
    )

/** Error banner state — typical for a missing API key or network failure. */
val previewAiChatBlocError: AiChatBloc =
    aiChatBloc(
        AiChatBloc.Model(
            messages = sampleConversation,
            canAddRecipe = true,
            error = FixedString("Gemini API key missing. Set gemini.key in gradle properties."),
        )
    )

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun AiChatScreenPreview() {
    ChefMateTheme { previewAiChatBloc.Content() }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun AiChatScreenEmptyPreview() {
    ChefMateTheme { previewAiChatBlocEmpty.Content() }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun AiChatScreenStreamingPreview() {
    ChefMateTheme { previewAiChatBlocStreaming.Content() }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun AiChatScreenErrorPreview() {
    ChefMateTheme { previewAiChatBlocError.Content() }
}
