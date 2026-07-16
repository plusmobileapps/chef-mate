package com.plusmobileapps.chefmate.ui.text

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Toggles an inline [marker] (e.g. [BOLD_MARKER] or [ITALIC_MARKER]) around the current selection:
 * - If the selection is already wrapped in [marker], it is unwrapped and the inner text
 *   re-selected.
 * - Otherwise the selection is wrapped and the inner text re-selected.
 * - With an empty selection, an empty marker pair is inserted and the cursor placed between the
 *   markers, ready for the user to type formatted text.
 */
fun TextFieldValue.toggleInlineMarker(marker: String): TextFieldValue {
    val start = minOf(selection.start, selection.end)
    val end = maxOf(selection.start, selection.end)
    val before = text.substring(0, start)
    val selected = text.substring(start, end)
    val after = text.substring(end)

    val alreadyWrapped =
        selected.length >= marker.length * 2 &&
            selected.startsWith(marker) &&
            selected.endsWith(marker)

    return if (alreadyWrapped) {
        val inner = selected.substring(marker.length, selected.length - marker.length)
        TextFieldValue(
            text = before + inner + after,
            selection = TextRange(start, start + inner.length),
        )
    } else {
        val innerStart = start + marker.length
        TextFieldValue(
            text = before + marker + selected + marker + after,
            selection = TextRange(innerStart, innerStart + selected.length),
        )
    }
}

/**
 * Replaces the current selection with a `[label](url)` markdown link and places the cursor just
 * after it. With an empty selection the link is inserted at the caret.
 */
fun TextFieldValue.insertMarkdownLink(label: String, url: String): TextFieldValue {
    val start = minOf(selection.start, selection.end)
    val end = maxOf(selection.start, selection.end)
    val markdown = "[$label]($url)"
    val newText = text.substring(0, start) + markdown + text.substring(end)
    val cursor = start + markdown.length
    return TextFieldValue(text = newText, selection = TextRange(cursor))
}
