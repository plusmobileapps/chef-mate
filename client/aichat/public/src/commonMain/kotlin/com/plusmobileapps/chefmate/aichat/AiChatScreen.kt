package com.plusmobileapps.chefmate.aichat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chefmate.client.aichat.public.generated.resources.Res
import chefmate.client.aichat.public.generated.resources.aichat_add_recipe
import chefmate.client.aichat.public.generated.resources.aichat_attach_photo
import chefmate.client.aichat.public.generated.resources.aichat_close
import chefmate.client.aichat.public.generated.resources.aichat_done
import chefmate.client.aichat.public.generated.resources.aichat_empty_description
import chefmate.client.aichat.public.generated.resources.aichat_empty_title
import chefmate.client.aichat.public.generated.resources.aichat_extracting_recipe
import chefmate.client.aichat.public.generated.resources.aichat_history
import chefmate.client.aichat.public.generated.resources.aichat_input_hint
import chefmate.client.aichat.public.generated.resources.aichat_new_chat
import chefmate.client.aichat.public.generated.resources.aichat_recipe_context
import chefmate.client.aichat.public.generated.resources.aichat_role_gemini
import chefmate.client.aichat.public.generated.resources.aichat_role_you
import chefmate.client.aichat.public.generated.resources.aichat_send
import chefmate.client.aichat.public.generated.resources.aichat_title
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.rememberMarkdownState
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.isIosPlatform
import com.plusmobileapps.chefmate.util.rememberImagePickerLauncher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun AiChatScreen(bloc: AiChatBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    // When hosted in the recipe-grounded sheet at its collapsed detent, render just the input (plus
    // the recipe chip) so the sheet hugs a small "peek" over the dimmed recipe. Tapping, focusing,
    // or dragging up expands the sheet to the full-screen chat below.
    if (LocalAiChatPresentation.current == AiChatPresentation.SheetPeek) {
        AiChatPeek(bloc = bloc, state = state, modifier = modifier)
        return
    }

    // In the expanded sheet there's no navigation stack to pop, so the app bar drops the back
    // arrow and instead carries History plus a Close (X) on the right; the X dismisses the whole
    // sheet (onBackClicked finishes the chat, which the host tears down). The standalone,
    // full-screen chat (More tab) keeps its back arrow.
    val headerData =
        if (LocalAiChatPresentation.current == AiChatPresentation.SheetExpanded) {
            PlusHeaderData.Parent(
                title = Res.string.aichat_title.asTextData(),
                trailingAccessory =
                    PlusHeaderData.TrailingAccessory.Custom {
                        HistoryButton(onClick = bloc::onHistoryClick)
                        IconButton(
                            onClick = bloc::onBackClicked,
                            modifier = Modifier.testTag(AiChatTestTags.CLOSE_BUTTON),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.aichat_close),
                            )
                        }
                    },
            )
        } else {
            PlusHeaderData.Child(
                title = Res.string.aichat_title.asTextData(),
                onBackClick = bloc::onBackClicked,
                trailingAccessory =
                    PlusHeaderData.TrailingAccessory.Custom {
                        HistoryButton(onClick = bloc::onHistoryClick)
                    },
            )
        }

    PlusHeaderContainer(
        modifier = modifier.fillMaxSize().testTag(AiChatTestTags.SCREEN),
        data = headerData,
        scrollEnabled = false,
        content = {
            state.recipeContextTitle?.let { title ->
                RecipeContextChip(
                    title = title,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
                )
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (state.messages.isEmpty()) {
                    EmptyState(modifier = Modifier.fillMaxSize())
                } else {
                    MessageList(
                        messages = state.messages,
                        isSending = state.isSending,
                        modifier = Modifier.fillMaxSize(),
                    )
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

/** Opens the conversation-history list; shared by the standalone and expanded-sheet app bars. */
@Composable
private fun HistoryButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.testTag(AiChatTestTags.HISTORY_BUTTON)) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = stringResource(Res.string.aichat_history),
        )
    }
}

/** Starts a fresh conversation for the current recipe. */
@Composable
private fun NewChatButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.testTag(AiChatTestTags.NEW_CHAT_BUTTON)) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(Res.string.aichat_new_chat),
        )
    }
}

/**
 * A short, truncated excerpt of the most recent turns, shown in the peek when the recipe already
 * has a conversation. Deliberately compact (a couple of clipped lines each) so the sheet stays
 * small; dragging up expands to the full chat.
 */
@Composable
private fun PeekExcerpt(messages: List<ChatMessage>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.testTag(AiChatTestTags.PEEK_EXCERPT),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        messages.takeLast(PEEK_EXCERPT_MESSAGE_COUNT).forEach { message ->
            val isUser = message.role == ChatMessage.Role.USER
            val label =
                if (isUser) stringResource(Res.string.aichat_role_you)
                else stringResource(Res.string.aichat_role_gemini)
            Text(
                text = "$label: ${message.content}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Collapsed "peek" presentation used by the recipe-grounded sheet: a compact action bar (history +
 * close) over the recipe-context chip and the input, so the sheet stays small over the dimmed
 * recipe while the user types. It grows to the full-screen chat only when the strip is dragged up,
 * or when a message is sent. Opening the sheet focuses the input and raises the keyboard right
 * away.
 */
@Composable
private fun AiChatPeek(bloc: AiChatBloc, state: AiChatBloc.Model, modifier: Modifier = Modifier) {
    val onExpand = LocalAiChatRequestExpand.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    var dragAccumulation by remember { mutableStateOf(0f) }

    // Opening the recipe-grounded sheet drops the user straight into typing: focus the input and
    // raise the keyboard as soon as the peek composes.
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
        keyboardController?.show()
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(AiChatTestTags.PEEK)
                .draggable(
                    orientation = Orientation.Vertical,
                    state =
                        rememberDraggableState { delta ->
                            dragAccumulation += delta
                            if (dragAccumulation < PEEK_DRAG_EXPAND_THRESHOLD) {
                                dragAccumulation = 0f
                                onExpand()
                            }
                        },
                    onDragStopped = { dragAccumulation = 0f },
                )
    ) {
        // Compact action bar: history, new conversation, and close — mirroring the expanded sheet's
        // app-bar accessories.
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HistoryButton(onClick = bloc::onHistoryClick)
            NewChatButton(onClick = bloc::onNewChatClick)
            IconButton(
                onClick = bloc::onBackClicked,
                modifier = Modifier.testTag(AiChatTestTags.CLOSE_BUTTON),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.aichat_close),
                )
            }
        }
        state.recipeContextTitle?.let { title ->
            RecipeContextChip(
                title = title,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
            )
        }
        // When the recipe already has a conversation, surface a short excerpt of its most recent
        // turns above the input so reopening lands on the conversation in progress. Dragging up on
        // it expands to the full chat.
        if (state.messages.isNotEmpty()) {
            PeekExcerpt(
                messages = state.messages,
                modifier =
                    Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 4.dp),
            )
        }
        AiChatInput(
            inputText = bloc.inputText,
            isSending = state.isSending,
            isExtracting = state.isExtractingRecipe,
            onInputChange = bloc::onInputChange,
            // The peek stays collapsed while the user types over the recipe — sending is what grows
            // it to the full conversation. Close the keyboard first, then expand a beat later so
            // the
            // keyboard-collapse and sheet-expand animations don't fight each other.
            onSendClick = {
                keyboardController?.hide()
                bloc.onSendClick()
                scope.launch {
                    delay(PEEK_SEND_EXPAND_DELAY_MS)
                    onExpand()
                }
            },
            onPhotoPicked = bloc::onPhotoPicked,
            focusRequester = focusRequester,
        )
    }
}

/** Upward drag (negative delta) past this many pixels on the peek strip expands the sheet. */
private const val PEEK_DRAG_EXPAND_THRESHOLD = -40f

/**
 * After sending from the peek, wait this long — enough for the keyboard to start collapsing —
 * before expanding the sheet, so the two animations don't run on top of each other.
 */
private const val PEEK_SEND_EXPAND_DELAY_MS = 100L

/** How many of the most recent messages the peek shows as a preview excerpt. */
private const val PEEK_EXCERPT_MESSAGE_COUNT = 2

@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    isSending: Boolean,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val inspection = LocalInspectionMode.current
    val last = messages.lastOrNull()

    // Until the reply's first token arrives no model row exists yet (the repository defers it), so
    // surface a "Gemini is thinking" bubble where the reply will land — at the start/bottom of the
    // log — instead of relying on the tiny spinner that used to live in the send button.
    val showThinking =
        isSending && messages.none { it.role == ChatMessage.Role.MODEL && it.isStreaming }

    // Capture, the moment the last message first appears, whether it began streaming this session.
    // A reply that streams in is spelled out line by line; an already-complete message (history
    // opened later, or any user message) renders in full immediately.
    val revealStreaming =
        remember(last?.id) {
            last != null && last.role == ChatMessage.Role.MODEL && last.isStreaming && !inspection
        }
    val revealedLength =
        rememberRevealedLength(
            id = last?.id,
            content = if (revealStreaming) last!!.content else "",
            enabled = revealStreaming,
        )

    // The list is anchored to its bottom edge (reverseLayout) with the newest message first, so as
    // the streaming reply grows it pushes older messages up while the latest text stays pinned just
    // above the input. The bottom tracks the growth purely through layout — there is no manual
    // scrolling, which is what made the previous auto-scroll snap and stutter. Scrolling up to read
    // earlier messages still works and is never yanked back down. No animateItem(): letting bubbles
    // reposition instantly as the reply grows is smooth, whereas animating every per-line shift was
    // its own source of jank.
    LazyColumn(
        state = listState,
        modifier = modifier.testTag(AiChatTestTags.MESSAGE_LIST),
        reverseLayout = true,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // reverseLayout: the first item renders at the bottom (newest), so the thinking bubble sits
        // just below the latest user message.
        if (showThinking) {
            item(key = "thinking") { ThinkingBubble() }
        }
        items(messages.asReversed(), key = { it.id }) { message ->
            val isRevealing = revealStreaming && message.id == last?.id
            val displayContent =
                if (isRevealing) message.content.take(revealedLength) else message.content
            val stillTyping = isRevealing && revealedLength < message.content.length
            val typingNow = message.isStreaming || stillTyping
            MessageBubble(
                message = message,
                displayContent = displayContent,
                showProgress = typingNow && displayContent.isNotEmpty(),
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    displayContent: String,
    showProgress: Boolean,
    modifier: Modifier = Modifier,
) {
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

    // Skip the entrance animation under @Preview / screenshot tests so a single captured frame
    // renders the final state deterministically.
    val inspection = LocalInspectionMode.current

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
                        // Render markdown live as the reply types in. retainState keeps the
                        // previously parsed content on screen while the next (background) parse
                        // runs, instead of dropping to the empty loading state on every update —
                        // that empty-frame swap on each delta is what made the reply flicker.
                        val markdownState =
                            rememberMarkdownState(content = displayText, retainState = true)
                        Markdown(
                            markdownState = markdownState,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                    if (showProgress) {
                        Spacer(modifier = Modifier.size(6.dp))
                        PlusLoadingIndicator(
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
 * Shown in the Gemini (start) position while a reply is in flight but no token has arrived yet.
 * Mirrors a model [MessageBubble] — same role label and bubble styling — with a small spinner in
 * place of text, so it reads as "Gemini is thinking" rather than a stray indicator on the input.
 */
@Composable
private fun ThinkingBubble(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text(
            text = stringResource(Res.string.aichat_role_gemini),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag(AiChatTestTags.THINKING_INDICATOR),
        ) {
            PlusLoadingIndicator(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp).size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Progressively reveals [content] for a smooth typewriter effect while a reply streams in. A few
 * characters are revealed each frame; the bubble re-parses this growing substring as markdown, but
 * that parse runs in the background and is conflated (and rendered with retainState so the previous
 * content stays on screen), so the reveal stays smooth.
 *
 * The reveal keeps its own pace independent of how fast deltas arrive: it speeds up as the backlog
 * grows so it never falls too far behind a fast stream, and once the message stops streaming it
 * finishes the remaining text and the loop terminates (no idle poll loop, which keeps UI tests
 * honest). When [enabled] is false the full length is returned immediately (history + user
 * messages).
 */
@Composable
private fun rememberRevealedLength(id: Long?, content: String, enabled: Boolean): Int {
    if (!enabled) return content.length
    var revealed by remember(id) { mutableIntStateOf(0) }
    // Re-key on [id]/[content] so a new reply resets the count and each streamed delta resumes the
    // reveal from where it left off, terminating once caught up.
    LaunchedEffect(id, content) {
        while (revealed < content.length) {
            revealed =
                (revealed + revealStep(content.length - revealed)).coerceAtMost(content.length)
            if (revealed < content.length) delay(REVEAL_FRAME_MS)
        }
    }
    return revealed
}

/** Frame cadence for the typewriter reveal (~60fps). */
private const val REVEAL_FRAME_MS = 16L

/** Characters revealed per frame, scaling up with the backlog so a fast stream stays caught up. */
private fun revealStep(backlog: Int): Int =
    when {
        backlog > 800 -> 12
        backlog > 400 -> 6
        backlog > 120 -> 3
        backlog > 30 -> 2
        else -> 1
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
                    PlusLoadingIndicator(
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

/**
 * Chip surfacing the recipe this chat is grounded in. Purely informational (non-clickable) — it
 * mirrors the recipe context that is also folded into the prompt so the user can see what Gemini is
 * answering about.
 */
@Composable
private fun RecipeContextChip(title: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Start) {
        AssistChip(
            onClick = {},
            enabled = false,
            modifier = Modifier.testTag(AiChatTestTags.RECIPE_CONTEXT_CHIP),
            label = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = stringResource(Res.string.aichat_recipe_context),
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                )
            },
            colors =
                AssistChipDefaults.assistChipColors(
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                    disabledLeadingIconContentColor = MaterialTheme.colorScheme.primary,
                ),
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
    focusRequester: FocusRequester? = null,
) {
    val text by inputText.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isIos = isIosPlatform()
    val presentation = LocalAiChatPresentation.current
    var isFocused by remember { mutableStateOf(false) }
    val canSend = text.isNotBlank() && !isSending
    val pickPhoto = rememberImagePickerLauncher { picked ->
        picked?.let { onPhotoPicked(it.bytes, it.fileExtension) }
    }

    // Inside the recipe-grounded sheet, Material3's ModalBottomSheet already applies imePadding()
    // to
    // its content — and consumes the ime inset — so it has lifted the whole sheet above the
    // keyboard
    // by the time we get here. A windowInsetsPadding of ime/navigationBars therefore can't see the
    // keyboard (ime reads as consumed-to-zero) and would just add the raw nav-bar height, leaving a
    // gap floating above the keyboard. Read the *raw* ime height instead to tell whether the
    // keyboard is up: while it is, add nothing; once it's down, reserve the nav bar / home
    // indicator.
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val navInsets = WindowInsets.navigationBars
    val sheetBottomPadding by
        remember(density, imeInsets, navInsets) {
            derivedStateOf {
                with(density) {
                    if (imeInsets.getBottom(density) > 0) 0.dp
                    else navInsets.getBottom(density).toDp()
                }
            }
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .then(
                    if (presentation == AiChatPresentation.FullScreen) {
                        // Standalone full-screen chat: nothing else handles the keyboard, so lift
                        // the input above whichever is larger — the keyboard or the navigation bar.
                        Modifier.windowInsetsPadding(
                            WindowInsets.ime.union(WindowInsets.navigationBars)
                        )
                    } else {
                        Modifier.padding(bottom = sheetBottomPadding)
                    }
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = pickPhoto,
            enabled = !isExtracting,
            modifier = Modifier.testTag(AiChatTestTags.ATTACH_PHOTO_BUTTON),
        ) {
            if (isExtracting) {
                PlusLoadingIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
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
                    .then(
                        if (focusRequester != null) Modifier.focusRequester(focusRequester)
                        else Modifier
                    )
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
                // The send button is purely enable/disable now — it lights up once there's text to
                // send. The in-flight indicator lives in the chat log (see ThinkingBubble) where
                // it's actually visible.
                IconButton(
                    onClick = onSendClick,
                    enabled = canSend,
                    modifier = Modifier.testTag(AiChatTestTags.SEND_BUTTON),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(Res.string.aichat_send),
                    )
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
