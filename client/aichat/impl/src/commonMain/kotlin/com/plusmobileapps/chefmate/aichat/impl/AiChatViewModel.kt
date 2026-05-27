package com.plusmobileapps.chefmate.aichat.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.aichat.AiChatBloc
import com.plusmobileapps.chefmate.aichat.AiChatGenericError
import com.plusmobileapps.chefmate.aichat.AiChatNoApiKeyError
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.text.TextData
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Inject
class AiChatViewModel(
    @Main mainContext: CoroutineContext,
    private val repository: AiChatRepository,
) : ViewModel(mainContext) {

    private val _inputText = MutableStateFlow("")
    private val _isSending = MutableStateFlow(false)
    private val _error = MutableStateFlow<TextData?>(null)

    val inputText: StateFlow<String> = _inputText.asStateFlow()

    val state: StateFlow<AiChatBloc.Model> =
        combine(repository.observeMessages(), _isSending, _error) { messages, sending, error ->
                AiChatBloc.Model(messages = messages, isSending = sending, error = error)
            }
            .stateIn(scope, SharingStarted.Eagerly, AiChatBloc.Model())

    fun onInputChange(text: String) {
        _inputText.value = text
        if (_error.value != null) _error.value = null
    }

    fun send() {
        val message = _inputText.value.trim()
        if (message.isEmpty() || _isSending.value) return
        _inputText.value = ""
        _error.value = null
        scope.launch {
            _isSending.value = true
            try {
                repository.sendMessage(message)
            } catch (e: GeminiException) {
                _error.value =
                    if (e.message == "MISSING_API_KEY") AiChatNoApiKeyError else AiChatGenericError
            } catch (_: Throwable) {
                _error.value = AiChatGenericError
            } finally {
                _isSending.value = false
            }
        }
    }

    fun clear() {
        if (_isSending.value) return
        scope.launch {
            repository.clearHistory()
            _error.value = null
        }
    }
}
