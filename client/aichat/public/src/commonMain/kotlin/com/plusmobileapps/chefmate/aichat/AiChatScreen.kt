package com.plusmobileapps.chefmate.aichat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.aichat.public.generated.resources.Res
import chefmate.client.aichat.public.generated.resources.aichat_clear
import chefmate.client.aichat.public.generated.resources.aichat_done
import chefmate.client.aichat.public.generated.resources.aichat_empty_description
import chefmate.client.aichat.public.generated.resources.aichat_empty_title
import chefmate.client.aichat.public.generated.resources.aichat_input_hint
import chefmate.client.aichat.public.generated.resources.aichat_role_gemini
import chefmate.client.aichat.public.generated.resources.aichat_role_you
import chefmate.client.aichat.public.generated.resources.aichat_send
import chefmate.client.aichat.public.generated.resources.aichat_title
import com.mikepenz.markdown.m3.Markdown
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.isIosPlatform
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

@Composable
fun AiChatScreen(bloc: AiChatBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    PlusHeaderContainer(
        modifier = modifier.fillMaxSize().testTag(AiChatTestTags.SCREEN),
        data =
            PlusHeaderData.Child(
                title = Res.string.aichat_title.asTextData(),
                onBackClick = bloc::onBackClicked,
                trailingAccessory =
                    PlusHeaderData.TrailingAccessory.Icon(
                        icon = Icons.Default.DeleteSweep,
                        contentDesc = Res.string.aichat_clear.asTextData(),
                        onClick = bloc::onClearClick,
                    ),
            ),
        scrollEnabled = false,
        content = {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (state.messages.isEmpty()) {
                    EmptyState(modifier = Modifier.fillMaxSize())
                } else {
                    MessageList(messages = state.messages, modifier = Modifier.fillMaxSize())
                }
            }
            state.error?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = error.localized(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
            AiChatInput(
                inputText = bloc.inputText,
                isSending = state.isSending,
                onInputChange = bloc::onInputChange,
                onSendClick = bloc::onSendClick,
            )
        },
    )
}

@Composable
private fun MessageList(messages: List<ChatMessage>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, messages.lastOrNull()?.content?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
    LazyColumn(
        state = listState,
        modifier = modifier.testTag(AiChatTestTags.MESSAGE_LIST),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(messages, key = { it.id }) { message -> MessageBubble(message = message) }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    val isUser = message.role == ChatMessage.Role.USER
    val bubbleColor =
        if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor =
        if (isUser) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val roleLabel =
        if (isUser) stringResource(Res.string.aichat_role_you)
        else stringResource(Res.string.aichat_role_gemini)
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Text(
            text = roleLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        )
        Surface(
            color = bubbleColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 520.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                val displayText =
                    if (message.isStreaming && message.content.isEmpty()) "…" else message.content
                if (isUser) {
                    Text(text = displayText, style = MaterialTheme.typography.bodyLarge)
                } else {
                    Markdown(content = displayText, modifier = Modifier.weight(1f, fill = false))
                }
                if (message.isStreaming && message.content.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(6.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        strokeWidth = 1.5.dp,
                        color = contentColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.aichat_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.aichat_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AiChatInput(
    inputText: StateFlow<String>,
    isSending: Boolean,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val text by inputText.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isIos = isIosPlatform()
    var isFocused by remember { mutableStateOf(false) }
    val canSend = text.isNotBlank() && !isSending

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onInputChange,
            modifier =
                Modifier.weight(1f)
                    .onFocusChanged { isFocused = it.isFocused }
                    .testTag(AiChatTestTags.INPUT),
            placeholder = { Text(stringResource(Res.string.aichat_input_hint)) },
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send,
                ),
            keyboardActions =
                KeyboardActions(
                    onSend = {
                        if (canSend) {
                            onSendClick()
                            keyboardController?.hide()
                        }
                    }
                ),
            trailingIcon = {
                IconButton(
                    onClick = onSendClick,
                    enabled = canSend,
                    modifier = Modifier.testTag(AiChatTestTags.SEND_BUTTON),
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(Res.string.aichat_send),
                        )
                    }
                }
            },
        )
        AnimatedVisibility(
            visible = isIos && isFocused,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            ) {
                Text(stringResource(Res.string.aichat_done))
            }
        }
    }
}
