@file:OptIn(ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chefmate.client.ui.public.generated.resources.Res
import chefmate.client.ui.public.generated.resources.markdown_editor_bold_a11y
import chefmate.client.ui.public.generated.resources.markdown_editor_italic_a11y
import chefmate.client.ui.public.generated.resources.markdown_editor_mode_markdown
import chefmate.client.ui.public.generated.resources.markdown_editor_mode_rich_text
import chefmate.client.ui.public.generated.resources.markdown_editor_preview_empty
import chefmate.client.ui.public.generated.resources.markdown_editor_resize_handle_a11y
import chefmate.client.ui.public.generated.resources.markdown_editor_tab_preview
import chefmate.client.ui.public.generated.resources.markdown_editor_tab_write
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.OutlinedRichTextEditor
import com.plusmobileapps.chefmate.ui.text.BOLD_MARKER
import com.plusmobileapps.chefmate.ui.text.ITALIC_MARKER
import com.plusmobileapps.chefmate.ui.text.toInlineMarkdownAnnotatedString
import com.plusmobileapps.chefmate.ui.text.toggleInlineMarker
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * A shared, resizable editor for plain-text fields that carry inline markdown (`**bold**`,
 * `_italic_`). The caller stores plain markdown [String]; the editor owns only transient state
 * (cursor/selection, the Markdown write/preview sub-toggle, and the resize height).
 *
 * Two modes, switched by [richTextMode] (a persisted, app-wide preference hoisted to the caller):
 * - **Rich text** — a WYSIWYG editor (compose-rich-editor): Bold/Italic format the selection and
 *   the field renders the formatting inline, markers hidden. The more approachable mode on mobile.
 * - **Markdown** — edits the raw source with a Bold/Italic toolbar plus a GitHub-style
 *   Write/Preview toggle (the switch animates). Toolbar buttons are non-focusable so tapping one
 *   never clears the field's selection.
 *
 * A drag handle in the bottom-end corner resizes the editor between [minHeight] and [maxHeight].
 */
@Composable
fun PlusMarkdownEditor(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    richTextMode: Boolean,
    onRichTextModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 160.dp,
    maxHeight: Dp = 480.dp,
    initialHeight: Dp = 200.dp,
) {
    var heightDp by rememberSaveable { mutableStateOf(initialHeight.value) }
    var showPreview by rememberSaveable { mutableStateOf(false) }

    // ── Markdown-mode editing state ───────────────────────────────────────────────────────────
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    LaunchedEffect(value) {
        // Re-sync only when external text diverges, so typing keeps the caret.
        if (value != fieldValue.text) fieldValue = TextFieldValue(value, TextRange(value.length))
    }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    fun updateMarkdown(newValue: TextFieldValue) {
        fieldValue = newValue
        onValueChange(newValue.text)
    }

    fun applyMarkdownMarker(marker: String) {
        updateMarkdown(fieldValue.toggleInlineMarker(marker))
        scope.launch { runCatching { focusRequester.requestFocus() } }
    }

    // ── Rich-text-mode editing state ──────────────────────────────────────────────────────────
    val richTextState = rememberRichTextState()
    val latestValue by rememberUpdatedState(value)
    val latestOnValueChange by rememberUpdatedState(onValueChange)
    // Seed the initial content synchronously so it's present on the first frame (no empty flash).
    remember(richTextState) { richTextState.setMarkdown(value) }
    LaunchedEffect(value) {
        // Load external markdown into the rich editor; the equality guard avoids resetting the
        // caret
        // while the user is typing (we just emitted this exact value).
        if (richTextState.toMarkdown() != value) richTextState.setMarkdown(value)
    }
    LaunchedEffect(richTextState) {
        snapshotFlow { richTextState.annotatedString }
            .collect {
                val markdown = richTextState.toMarkdown()
                if (markdown != latestValue) latestOnValueChange(markdown)
            }
    }

    fun onBold() {
        if (richTextMode) richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
        else applyMarkdownMarker(BOLD_MARKER)
    }

    fun onItalic() {
        if (richTextMode) richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
        else applyMarkdownMarker(ITALIC_MARKER)
    }

    // A light rounded outline groups each editor as one unit, so it's clear which toolbar/toggle
    // belongs to which field when several editors stack on the form.
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.medium,
                )
                .padding(ChefMateTheme.dimens.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingExtraSmall),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            EditorModeToggle(
                richTextMode = richTextMode,
                onRichTextModeChange = onRichTextModeChange,
            )
        }

        // Toolbar row: Bold/Italic on the left (hidden only in Markdown preview), Write/Preview
        // toggle on the right (Markdown mode only).
        val showFormatButtons = richTextMode || !showPreview
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (showFormatButtons) {
                MarkdownFormatToolbar(onBold = ::onBold, onItalic = ::onItalic)
            }
            Spacer(modifier = Modifier.weight(1f))
            if (!richTextMode) {
                WritePreviewToggle(
                    showPreview = showPreview,
                    onShowPreviewChange = { showPreview = it },
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(heightDp.dp)) {
            if (richTextMode) {
                OutlinedRichTextEditor(
                    state = richTextState,
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                AnimatedContent(
                    targetState = showPreview,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                    label = "markdown-write-preview",
                    modifier = Modifier.fillMaxSize(),
                ) { preview ->
                    if (preview) {
                        MarkdownPreview(text = fieldValue.text, modifier = Modifier.fillMaxSize())
                    } else {
                        OutlinedTextField(
                            value = fieldValue,
                            onValueChange = ::updateMarkdown,
                            placeholder = { Text(placeholder) },
                            modifier = Modifier.fillMaxSize().focusRequester(focusRequester),
                        )
                    }
                }
            }
            ResizeHandle(
                label = label,
                modifier = Modifier.align(Alignment.BottomEnd),
                onResizeBy = { deltaDp ->
                    heightDp = (heightDp + deltaDp).coerceIn(minHeight.value, maxHeight.value)
                },
            )
        }
    }
}

@Composable
private fun EditorModeToggle(
    richTextMode: Boolean,
    onRichTextModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        SegmentedButton(
            selected = richTextMode,
            onClick = { onRichTextModeChange(true) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        ) {
            Text(stringResource(Res.string.markdown_editor_mode_rich_text))
        }
        SegmentedButton(
            selected = !richTextMode,
            onClick = { onRichTextModeChange(false) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        ) {
            Text(stringResource(Res.string.markdown_editor_mode_markdown))
        }
    }
}

@Composable
private fun WritePreviewToggle(
    showPreview: Boolean,
    onShowPreviewChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        SegmentedButton(
            selected = !showPreview,
            onClick = { onShowPreviewChange(false) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        ) {
            Text(stringResource(Res.string.markdown_editor_tab_write))
        }
        SegmentedButton(
            selected = showPreview,
            onClick = { onShowPreviewChange(true) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        ) {
            Text(stringResource(Res.string.markdown_editor_tab_preview))
        }
    }
}

@Composable
private fun MarkdownFormatToolbar(
    onBold: () -> Unit,
    onItalic: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingExtraSmall),
    ) {
        FormatButton(
            icon = Icons.Default.FormatBold,
            contentDescription = stringResource(Res.string.markdown_editor_bold_a11y),
            onClick = onBold,
        )
        FormatButton(
            icon = Icons.Default.FormatItalic,
            contentDescription = stringResource(Res.string.markdown_editor_italic_a11y),
            onClick = onItalic,
        )
    }
}

@Composable
private fun FormatButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    // Non-focusable so pressing it doesn't pull focus off the text field and collapse its
    // selection.
    IconButton(onClick = onClick, modifier = Modifier.focusProperties { canFocus = false }) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Composable
private fun MarkdownPreview(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.extraSmall,
                )
                .padding(ChefMateTheme.dimens.paddingNormal)
    ) {
        if (text.isBlank()) {
            Text(
                text = stringResource(Res.string.markdown_editor_preview_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingExtraSmall),
            ) {
                text.split("\n").forEach { line ->
                    Text(
                        text = line.toInlineMarkdownAnnotatedString(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResizeHandle(label: String, modifier: Modifier, onResizeBy: (Float) -> Unit) {
    Icon(
        imageVector = Icons.Default.DragHandle,
        contentDescription = stringResource(Res.string.markdown_editor_resize_handle_a11y, label),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            modifier.padding(ChefMateTheme.dimens.paddingSmall).size(20.dp).pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onResizeBy(dragAmount.y.toDp().value)
                }
            },
    )
}
