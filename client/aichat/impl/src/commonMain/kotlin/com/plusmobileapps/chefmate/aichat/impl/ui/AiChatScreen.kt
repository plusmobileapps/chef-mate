package com.plusmobileapps.chefmate.aichat.impl.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.aichat.public.generated.resources.Res
import chefmate.client.aichat.public.generated.resources.aichat_add_recipe
import chefmate.client.aichat.public.generated.resources.aichat_attach_photo
import chefmate.client.aichat.public.generated.resources.aichat_done
import chefmate.client.aichat.public.generated.resources.aichat_empty_description
import chefmate.client.aichat.public.generated.resources.aichat_empty_title
import chefmate.client.aichat.public.generated.resources.aichat_extracting_recipe
import chefmate.client.aichat.public.generated.resources.aichat_history
import chefmate.client.aichat.public.generated.resources.aichat_input_hint
import chefmate.client.aichat.public.generated.resources.aichat_role_gemini
import chefmate.client.aichat.public.generated.resources.aichat_role_you
import chefmate.client.aichat.public.generated.resources.aichat_send
import chefmate.client.aichat.public.generated.resources.aichat_title
import com.mikepenz.markdown.m3.Markdown
import com.plusmobileapps.chefmate.aichat.AiChatBloc
import com.plusmobileapps.chefmate.aichat.AiChatTestTags
import com.plusmobileapps.chefmate.aichat.ChatMessage
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.isIosPlatform
import com.plusmobileapps.chefmate.util.rememberImagePickerLauncher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

@Composable
fun AiChatScreen(bloc: AiChatBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    PlusHeaderContainer(
        modifier = modifier.fillMaxSize().imePadding().testTag(AiChatTestTags.SCREEN),
        data =
            PlusHeaderData.Child(
                title = Res.string.aichat_title.asTextData(),
                onBackClick = bloc::onBackClicked,
                trailingAccessory =
                    PlusHeaderData.TrailingAccessory.Custom {
                        IconButton(
                            onClick = bloc::onHistoryClick,
                            modifier = Modifier.testTag(AiChatTestTags.HISTORY_BUTTON),
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = stringResource(Res.string.aichat_history),
                            )
                        }
                    },
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
            if (state.canAddRecipe) {
                AddRecipePill(
                    isLoading = state.isExtractingRecipe,
                    onClick = bloc::onAddRecipeClick,
                    modifier =
                        Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 4.dp),
                )
            }
            AiChatInput(
                inputText = bloc.inputText,
                isSending = state.isSending,
                isExtracting = state.isExtractingRecipe,
                onInputChange = bloc::onInputChange,
                onSendClick = bloc::onSendClick,
                onPhotoPicked = bloc::onPhotoPicked,
            )
        },
    )
}

@Composable
private fun MessageList(messages: List<ChatMessage>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val lastIndex = messages.lastIndex
    val isStreaming = messages.lastOrNull()?.isStreaming == true
    // A newly appended message animates into view.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(lastIndex)
        }
    }
    // While the last message streams, jump to the bottom instantly on each
    // delta. Animating here would restart the scroll animation many times per
    // second, which is what makes the chat stutter as the response loads in.
    LaunchedEffect(messages.lastOrNull()?.content?.length) {
        if (isStreaming && messages.isNotEmpty()) {
            listState.scrollToItem(lastIndex)
        }
    }
    LazyColumn(
        state = listState,
        modifier = modifier.testTag(AiChatTestTags.MESSAGE_LIST),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubble(message = message, modifier = Modifier.animateItem())
        }
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

    // Skip the entrance + typewriter animations under @Preview / screenshot tests so a single
    // captured frame renders the final, fully-revealed state deterministically.
    val inspection = LocalInspectionMode.current

    // A model response that begins streaming this session is spelled out left to right,
    // line by line. History (already-complete) messages and user messages render in full.
    val typewriter = remember { !isUser && message.isStreaming }
    val displayContent =
        rememberRevealedContent(content = message.content, enabled = typewriter && !inspection)
    val isTyping = typewriter && displayContent.length < message.content.length

    // Slide + fade each bubble into the chat log as it is appended.
    val appearance = remember { MutableTransitionState(inspection).apply { targetState = true } }

    AnimatedVisibility(
        visibleState = appearance,
        modifier = modifier.fillMaxWidth(),
        enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 3 },
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
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
                    // While streaming with nothing revealed yet, show a thinking placeholder.
                    val displayText =
                        if (displayContent.isEmpty() && message.isStreaming) "…" else displayContent
                    if (isUser) {
                        Text(text = displayText, style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Markdown(
                            content = displayText,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                    if ((message.isStreaming || isTyping) && displayContent.isNotEmpty()) {
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
}

/**
 * Progressively reveals [content] one character at a time so a streamed response is spelled out
 * left to right. The reveal keeps its own pace independent of how fast deltas arrive — when the
 * model races ahead the display lags behind and catches up, speeding up only when the backlog
 * grows. Once the message stops streaming the reveal finishes the remaining text and stops.
 *
 * When [enabled] is false the full [content] is returned immediately (history + user messages).
 */
@Composable
private fun rememberRevealedContent(content: String, enabled: Boolean): String {
    if (!enabled) return content
    var revealed by remember { mutableIntStateOf(0) }
    // Re-key on [content] so each streamed delta resumes the reveal from where it left off and the
    // effect terminates once caught up — no idle-blocking poll loop, which keeps UI tests honest.
    LaunchedEffect(content) {
        while (revealed < content.length) {
            revealed = (revealed + 1).coerceAtMost(content.length)
            delay(revealDelayMillis(content.length - revealed))
        }
    }
    return content.take(revealed)
}

/**
 * Per-character delay for the typewriter reveal. A steady, readable cadence for short bursts that
 * shortens as the backlog grows so the display never falls too far behind a fast stream.
 */
private fun revealDelayMillis(backlog: Int): Long =
    when {
        backlog > 320 -> 3L
        backlog > 160 -> 6L
        backlog > 80 -> 10L
        backlog > 24 -> 16L
        else -> 24L
    }

@Composable
private fun AddRecipePill(isLoading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.End) {
        AssistChip(
            onClick = onClick,
            enabled = !isLoading,
            modifier = Modifier.testTag(AiChatTestTags.ADD_RECIPE_PILL),
            label = {
                Text(
                    text =
                        if (isLoading) stringResource(Res.string.aichat_extracting_recipe)
                        else stringResource(Res.string.aichat_add_recipe)
                )
            },
            leadingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                    )
                }
            },
        )
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
    isExtracting: Boolean,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onPhotoPicked: (ByteArray, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val text by inputText.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isIos = isIosPlatform()
    var isFocused by remember { mutableStateOf(false) }
    val canSend = text.isNotBlank() && !isSending
    val pickPhoto = rememberImagePickerLauncher { picked ->
        picked?.let { onPhotoPicked(it.bytes, it.fileExtension) }
    }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = pickPhoto,
            enabled = !isExtracting,
            modifier = Modifier.testTag(AiChatTestTags.ATTACH_PHOTO_BUTTON),
        ) {
            if (isExtracting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = stringResource(Res.string.aichat_attach_photo),
                )
            }
        }
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
