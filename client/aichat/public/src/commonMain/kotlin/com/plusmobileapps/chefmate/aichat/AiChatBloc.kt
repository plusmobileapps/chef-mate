package com.plusmobileapps.chefmate.aichat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface AiChatBloc : BackClickBloc, ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        AiChatScreen(bloc = this, modifier = modifier)
    }

    val inputText: StateFlow<String>

    fun onInputChange(text: String)

    fun onSendClick()

    fun onHistoryClick()

    fun onAddRecipeClick()

    /**
     * The user attached a photo to extract a recipe from. [fileExtension] is the picked file's
     * extension (e.g. `jpg`, `png`); the bloc derives the Gemini mime type from it and reuses the
     * same bytes as the recipe's pending image.
     */
    fun onPhotoPicked(bytes: ByteArray, fileExtension: String)

    data class Model(
        val messages: ImmutableList<ChatMessage> = persistentListOf(),
        val isSending: Boolean = false,
        val canAddRecipe: Boolean = false,
        val isExtractingRecipe: Boolean = false,
        val error: TextData? = null,
        /**
         * Title of the recipe this chat is grounded in, shown as a chip above the input. Non-null
         * only when the chat was opened from a recipe (Cook Mode or the recipe detail screen); the
         * recipe's details are also folded into the prompt so replies stay on-topic.
         */
        val recipeContextTitle: String? = null,
    )

    @Serializable
    sealed class Props {
        /**
         * A fresh chat — the conversation row is created lazily on first send. When
         * [recipeContextId] is set, the chat is seeded with that recipe as context (a chip plus a
         * prompt preamble) so the user can ask about or tweak it.
         */
        @Serializable data class NewConversation(val recipeContextId: Long? = null) : Props()

        /** Reopening an existing conversation by id. */
        @Serializable data class ExistingConversation(val conversationId: Long) : Props()
    }

    sealed class Output {
        data object Back : Output()

        data object OpenHistory : Output()

        data class AddAsRecipe(
            val extracted: ExtractedRecipeData,
            /** True when extraction came from a photo whose bytes should seed the recipe image. */
            val consumePendingPhoto: Boolean = false,
        ) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, props: Props, output: Consumer<Output>): AiChatBloc
    }
}
