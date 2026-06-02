package com.plusmobileapps.chefmate.aichat.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.aichat.AiChatBloc
import com.plusmobileapps.chefmate.aichat.AiChatExtractionError
import com.plusmobileapps.chefmate.aichat.AiChatGenericError
import com.plusmobileapps.chefmate.aichat.AiChatNoApiKeyError
import com.plusmobileapps.chefmate.aichat.ChatMessage
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.text.TextData
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class AiChatViewModel(
    @Main mainContext: CoroutineContext,
    @Assisted props: AiChatBloc.Props,
    private val repository: AiChatRepository,
    private val recipeExtractor: GeminiRecipeExtractor,
) : ViewModel(mainContext) {

    private val _conversationId =
        MutableStateFlow(
            when (props) {
                is AiChatBloc.Props.NewConversation -> null
                is AiChatBloc.Props.ExistingConversation -> props.conversationId
            }
        )
    private val _inputText = MutableStateFlow("")
    private val _isSending = MutableStateFlow(false)
    private val _isExtractingRecipe = MutableStateFlow(false)
    private val _error = MutableStateFlow<TextData?>(null)
    private val _extractedRecipe =
        MutableSharedFlow<ExtractedRecipeData>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    val inputText: StateFlow<String> = _inputText.asStateFlow()

    /**
     * Emits each time the user successfully extracts a recipe from the chat. Collected by the
     * BlocImpl so it can forward as an [AiChatBloc.Output.AddAsRecipe].
     */
    val extractedRecipe: SharedFlow<ExtractedRecipeData> = _extractedRecipe.asSharedFlow()

    private val messages = _conversationId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.observeMessages(id)
    }

    private val canAddRecipe = messages.map { list ->
        list.any { it.role == ChatMessage.Role.MODEL && !it.isStreaming }
    }

    val state: StateFlow<AiChatBloc.Model> =
        combine(messages, canAddRecipe, _isSending, _isExtractingRecipe, _error) {
                messages,
                canAdd,
                sending,
                extracting,
                error ->
                AiChatBloc.Model(
                    messages = messages,
                    isSending = sending,
                    canAddRecipe = canAdd,
                    isExtractingRecipe = extracting,
                    error = error,
                )
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
                val newId = repository.sendMessage(_conversationId.value, message)
                if (newId != null && _conversationId.value == null) {
                    _conversationId.value = newId
                }
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

    fun extractRecipe() {
        if (_isExtractingRecipe.value) return
        scope.launch {
            _isExtractingRecipe.value = true
            _error.value = null
            try {
                val history = messages.first()
                if (history.none { it.role == ChatMessage.Role.MODEL && !it.isStreaming })
                    return@launch
                val recipe = recipeExtractor.extract(history)
                _extractedRecipe.tryEmit(recipe)
            } catch (e: GeminiExtractionException) {
                _error.value =
                    if (e.message == "MISSING_API_KEY") AiChatNoApiKeyError
                    else AiChatExtractionError
            } catch (_: Throwable) {
                _error.value = AiChatExtractionError
            } finally {
                _isExtractingRecipe.value = false
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(props: AiChatBloc.Props): AiChatViewModel
    }
}
