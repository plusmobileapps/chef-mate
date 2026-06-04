package com.plusmobileapps.chefmate.aichat

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface AiChatBloc : BackClickBloc, BlocScreen {
    val state: StateFlow<Model>

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
        val messages: List<ChatMessage> = emptyList(),
        val isSending: Boolean = false,
        val canAddRecipe: Boolean = false,
        val isExtractingRecipe: Boolean = false,
        val error: TextData? = null,
    )

    @Serializable
    sealed class Props {
        /** A fresh chat — the conversation row is created lazily on first send. */
        @Serializable data object NewConversation : Props()

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
