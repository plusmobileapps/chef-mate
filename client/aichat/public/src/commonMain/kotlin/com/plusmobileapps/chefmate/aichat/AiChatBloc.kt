package com.plusmobileapps.chefmate.aichat

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.coroutines.flow.StateFlow

interface AiChatBloc : BackClickBloc, BlocScreen {
    val state: StateFlow<Model>

    val inputText: StateFlow<String>

    fun onInputChange(text: String)

    fun onSendClick()

    fun onClearClick()

    fun onAddRecipeClick()

    data class Model(
        val messages: List<ChatMessage> = emptyList(),
        val isSending: Boolean = false,
        val canAddRecipe: Boolean = false,
        val isExtractingRecipe: Boolean = false,
        val error: TextData? = null,
    )

    sealed class Output {
        data object Back : Output()

        data class AddAsRecipe(val extracted: ExtractedRecipeData) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): AiChatBloc
    }
}
