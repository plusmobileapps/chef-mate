package com.plusmobileapps.chefmate.ui.text

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MarkdownFormattingTest {

    @Test
    fun wrapsSelectionAndReselectsInner() {
        val value = TextFieldValue("preheat the oven", selection = TextRange(0, 7))

        val result = value.toggleInlineMarker(BOLD_MARKER)

        result.text shouldBe "**preheat** the oven"
        // Inner text stays selected so a second toggle can unwrap it.
        result.selection shouldBe TextRange(2, 9)
    }

    @Test
    fun emptySelectionInsertsMarkerPairWithCursorBetween() {
        val value = TextFieldValue("", selection = TextRange(0))

        val result = value.toggleInlineMarker(ITALIC_MARKER)

        result.text shouldBe "__"
        result.selection shouldBe TextRange(1, 1)
    }

    @Test
    fun toggleOnWrappedSelectionUnwraps() {
        val value = TextFieldValue("**preheat** the oven", selection = TextRange(0, 11))

        val result = value.toggleInlineMarker(BOLD_MARKER)

        result.text shouldBe "preheat the oven"
        result.selection shouldBe TextRange(0, 7)
    }

    @Test
    fun usesNormalizedSelectionBounds() {
        // Reversed selection (anchor after focus) should behave like a forward one.
        val value = TextFieldValue("preheat the oven", selection = TextRange(7, 0))

        val result = value.toggleInlineMarker(BOLD_MARKER)

        result.text shouldBe "**preheat** the oven"
        result.selection shouldBe TextRange(2, 9)
    }
}
