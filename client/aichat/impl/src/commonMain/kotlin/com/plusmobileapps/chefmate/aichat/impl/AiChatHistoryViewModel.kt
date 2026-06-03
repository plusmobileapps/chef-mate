package com.plusmobileapps.chefmate.aichat.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.aichat.AiChatConversation
import com.plusmobileapps.chefmate.di.Main
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Inject
class AiChatHistoryViewModel(
    @Main mainContext: CoroutineContext,
    private val repository: AiChatRepository,
) : ViewModel(mainContext) {

    val conversations: StateFlow<List<AiChatConversation>> =
        repository.observeConversations().stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun deleteConversation(id: Long) {
        scope.launch { repository.deleteConversation(id) }
    }

    fun deleteAllConversations() {
        scope.launch { repository.deleteAllConversations() }
    }
}
