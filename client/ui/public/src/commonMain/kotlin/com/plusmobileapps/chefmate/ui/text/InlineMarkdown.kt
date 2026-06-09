package com.plusmobileapps.chefmate.ui.text

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Marker for bold inline spans. Wrapping a selection in [BOLD_MARKER] renders it bold both in the
 * editor toolbar and when the recipe is displayed.
 */
const val BOLD_MARKER: String = "**"

/** Marker for italic inline spans. */
const val ITALIC_MARKER: String = "_"

/**
 * Parses a single line of lightweight inline markdown into an [AnnotatedString], supporting
 * `**bold**` and `_italic_` (nestable, e.g. `**bold _and italic_**`). Markers without a matching
 * partner are left as literal text, so plain recipe lines — and stray `*` or `_` characters —
 * render unchanged. This is intentionally inline-only: ingredients and directions stay one item per
 * line, so block markdown (headings, lists, etc.) is not interpreted.
 */
fun String.toInlineMarkdownAnnotatedString(): AnnotatedString = buildAnnotatedString {
    appendInlineMarkdown(this@toInlineMarkdownAnnotatedString)
}

private fun AnnotatedString.Builder.appendInlineMarkdown(text: String) {
    var literalStart = 0
    var i = 0
    while (i < text.length) {
        val marker = markerAt(text, i)
        if (marker == null) {
            i++
            continue
        }
        val contentStart = i + marker.length
        val close = text.indexOf(marker, contentStart)
        if (close == -1) {
            // No closing marker — treat this marker as literal text and keep scanning past it.
            i += marker.length
            continue
        }
        if (literalStart < i) append(text.substring(literalStart, i))
        withStyle(styleFor(marker)) { appendInlineMarkdown(text.substring(contentStart, close)) }
        i = close + marker.length
        literalStart = i
    }
    if (literalStart < text.length) append(text.substring(literalStart))
}

private fun markerAt(text: String, index: Int): String? =
    when {
        text.startsWith(BOLD_MARKER, index) -> BOLD_MARKER
        text.startsWith(ITALIC_MARKER, index) -> ITALIC_MARKER
        else -> null
    }

private fun styleFor(marker: String): SpanStyle =
    when (marker) {
        BOLD_MARKER -> SpanStyle(fontWeight = FontWeight.Bold)
        else -> SpanStyle(fontStyle = FontStyle.Italic)
    }
