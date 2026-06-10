@file:OptIn(ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Card
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chefmate.client.ui.public.generated.resources.Res
import chefmate.client.ui.public.generated.resources.markdown_editor_bold_a11y
import chefmate.client.ui.public.generated.resources.markdown_editor_italic_a11y
import chefmate.client.ui.public.generated.resources.markdown_editor_preview_empty
import chefmate.client.ui.public.generated.resources.markdown_editor_resize_handle_a11y
import chefmate.client.ui.public.generated.resources.markdown_editor_tab_preview
import chefmate.client.ui.public.generated.resources.markdown_editor_tab_write
import com.plusmobileapps.chefmate.ui.text.BOLD_MARKER
import com.plusmobileapps.chefmate.ui.text.ITALIC_MARKER
import com.plusmobileapps.chefmate.ui.text.toInlineMarkdownAnnotatedString
import com.plusmobileapps.chefmate.ui.text.toggleInlineMarker
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * A shared, resizable rich-text editor for plain-text fields that carry inline markdown
 * (`**bold**`, `_italic_`). Two modes via a segmented toggle:
 * - **Markdown** — edits the raw source with a bold/italic toolbar that formats the current
 *   selection. The toolbar buttons are non-focusable so tapping one never clears the text field's
 *   selection (which would otherwise insert empty markers next to the highlight).
 * - **Preview** — read-only rendered view of the same content.
 *
 * A drag handle in the bottom-end corner resizes the editor between [minHeight] and [maxHeight];
 * the chosen height and selected tab survive configuration changes. The bloc/caller still stores a
 * plain [String]; this component owns only the transient cursor/selection and view state.
 */
@Composable
fun PlusMarkdownEditor(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 160.dp,
    maxHeight: Dp = 480.dp,
    initialHeight: Dp = 200.dp,
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    // Re-sync only when the external text diverges (async load, discard) so typing keeps the caret.
    LaunchedEffect(value) {
        if (value != fieldValue.text) fieldValue = TextFieldValue(value, TextRange(value.length))
    }
    var heightDp by rememberSaveable { mutableStateOf(initialHeight.value) }
    var showPreview by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    fun update(newValue: TextFieldValue) {
        fieldValue = newValue
        onValueChange(newValue.text)
    }

    fun applyFormat(marker: String) {
        update(fieldValue.toggleInlineMarker(marker))
        // Keep the field focused so the re-selected text stays visible for a follow-up toggle.
        scope.launch { runCatching { focusRequester.requestFocus() } }
    }

    // Carded so each editor reads as one self-contained unit — it's otherwise easy to lose track of
    // which toolbar/toggle belongs to which field when several stack together on the form.
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingNormal),
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
                MarkdownModeToggle(
                    showPreview = showPreview,
                    onShowPreviewChange = { showPreview = it },
                )
            }

            if (!showPreview) {
                MarkdownFormatToolbar(
                    onBold = { applyFormat(BOLD_MARKER) },
                    onItalic = { applyFormat(ITALIC_MARKER) },
                )
            }

            Box(modifier = Modifier.fillMaxWidth().height(heightDp.dp)) {
                if (showPreview) {
                    MarkdownPreview(text = fieldValue.text, modifier = Modifier.fillMaxSize())
                } else {
                    OutlinedTextField(
                        value = fieldValue,
                        onValueChange = ::update,
                        placeholder = { Text(placeholder) },
                        modifier = Modifier.fillMaxSize().focusRequester(focusRequester),
                    )
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
}

@Composable
private fun MarkdownModeToggle(
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
