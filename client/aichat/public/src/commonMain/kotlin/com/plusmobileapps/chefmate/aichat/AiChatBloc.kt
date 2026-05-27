package com.plusmobileapps.chefmate.aichat

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.text.TextData
import kotlinx.coroutines.flow.StateFlow

interface AiChatBloc : BackClickBloc {
    val state: StateFlow<Model>

    val inputText: StateFlow<String>

    fun onInputChange(text: String)

    fun onSendClick()

    fun onClearClick()

    data class Model(
        val messages: List<ChatMessage> = emptyList(),
        val isSending: Boolean = false,
        val error: TextData? = null,
    )

    sealed class Output {
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): AiChatBloc
    }
}
