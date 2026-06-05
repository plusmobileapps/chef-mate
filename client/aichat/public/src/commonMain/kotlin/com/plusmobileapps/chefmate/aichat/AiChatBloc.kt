package com.plusmobileapps.chefmate.aichat

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface AiChatBloc : BackClickBloc, BlocScreen {
    val state: StateFlow<Model>

    val inputText: StateFlow<String>

    fun onInputChange(text: String)

    fun onSendClick()

    fun onHistoryClick()

    fun onAddRecipeClick()

    data class Model(
        val messages: ImmutableList<ChatMessage> = persistentListOf(),
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

        data class AddAsRecipe(val extracted: ExtractedRecipeData) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, props: Props, output: Consumer<Output>): AiChatBloc
    }
}
