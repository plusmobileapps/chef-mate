package com.plusmobileapps.chefmate.aichat

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface AiChatHistoryBloc : BackClickBloc, BlocScreen {
    val state: StateFlow<Model>

    fun onConversationClick(conversationId: Long)

    fun onDeleteConversationClick(conversationId: Long)

    fun onDeleteAllClick()

    fun onNewConversationClick()

    data class Model(val conversations: ImmutableList<AiChatConversation> = persistentListOf())

    sealed class Output {
        data object Back : Output()

        data class OpenConversation(val conversationId: Long) : Output()

        data object NewConversation : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): AiChatHistoryBloc
    }
}
