package com.plusmobileapps.chefmate.ui.text

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * The prefix inserted ahead of a line to mark it as a bulleted list item. Recipe lines stay
 * one-item-per-line, so lists are expressed as per-line prefixes rather than block markdown.
 */
const val BULLET_MARKER: String = "- "

/** The glyph shown in place of the raw [BULLET_MARKER] when a bulleted line is rendered. */
const val BULLET_GLYPH: String = "•  "

// A leading bullet marker: "- item" or "* item" (a single dash/asterisk followed by whitespace).
// A single "*" can't be confused with the "**" bold marker, whose second char is never whitespace.
private val bulletPrefix = Regex("""^\s*[-*]\s+""")

// A leading ordered enumerator the author typed: "1. ", "2) ", etc.
private val orderedPrefix = Regex("""^\s*(\d+)[.)]\s+""")

/** How a single line is marked as a list item, if at all. */
sealed interface LineMarker {
    /** A bulleted item — rendered with a [BULLET_GLYPH]. */
    data object Bullet : LineMarker

    /** An ordered item — rendered with the author's own [number], never renumbered. */
    data class Ordered(val number: Int) : LineMarker

    /** A plain line with no list marker. */
    data object None : LineMarker
}

/** A line split into its list [marker] and the remaining [content] (marker stripped). */
data class ListLine(val marker: LineMarker, val content: String)

/**
 * Classifies a single line by its leading list marker. Bullets (`- `/`* `) and ordered enumerators
 * (`1. `/`2) `) are recognized and stripped from [ListLine.content]; anything else is
 * [LineMarker.None] with the line unchanged. Numbers are only ever the author's own — nothing here
 * generates them.
 */
fun parseListLine(line: String): ListLine {
    bulletPrefix.find(line)?.let { match ->
        return ListLine(LineMarker.Bullet, line.removeRange(match.range))
    }
    orderedPrefix.find(line)?.let { match ->
        return ListLine(
            LineMarker.Ordered(match.groupValues[1].toIntOrNull() ?: 0),
            line.removeRange(match.range),
        )
    }
    return ListLine(LineMarker.None, line)
}

/**
 * Renders a [ListLine] to an [AnnotatedString]: a bold `•`/`N.` prefix for list markers, followed by
 * the [ListLine.content] with inline markdown (`**bold**`, `_italic_`, `[label](url)`) applied.
 * [linkStyle] and [onLinkClick] are forwarded to [toInlineMarkdownAnnotatedString], so recipe links
 * stay clickable on the surfaces that support them. Callers that need their own styling
 * (strikethrough, highlight) wrap the result.
 */
fun ListLine.toDisplayAnnotatedString(
    linkStyle: SpanStyle? = null,
    onLinkClick: ((String) -> Unit)? = null,
): AnnotatedString = buildAnnotatedString {
    when (val current = marker) {
        LineMarker.Bullet ->
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(BULLET_GLYPH) }
        is LineMarker.Ordered ->
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("${current.number}. ") }
        LineMarker.None -> {}
    }
    append(content.toInlineMarkdownAnnotatedString(linkStyle, onLinkClick))
}
