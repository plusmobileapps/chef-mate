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

    @Test
    fun insertMarkdownLinkAtCursorPlacesLinkAndMovesCursorAfter() {
        val value = TextFieldValue("serve with ", selection = TextRange(11))

        val result = value.insertMarkdownLink("Green Sauce", "chefmate://recipe/abc")

        result.text shouldBe "serve with [Green Sauce](chefmate://recipe/abc)"
        result.selection shouldBe TextRange(result.text.length)
    }

    @Test
    fun insertMarkdownLinkReplacesSelection() {
        val value = TextFieldValue("serve with sauce", selection = TextRange(11, 16))

        val result = value.insertMarkdownLink("Green Sauce", "chefmate://recipe/abc")

        result.text shouldBe "serve with [Green Sauce](chefmate://recipe/abc)"
        result.selection shouldBe TextRange(result.text.length)
    }

    @Test
    fun bulletsEachSelectedLine() {
        val value = TextFieldValue("flour\nsugar\neggs", selection = TextRange(0, 16))

        val result = value.toggleBulletList()

        result.text shouldBe "- flour\n- sugar\n- eggs"
    }

    @Test
    fun bulletsTheCurrentLineWithNoSelection() {
        // Caret on the middle line, nothing selected — only that line gets a bullet.
        val value = TextFieldValue("flour\nsugar\neggs", selection = TextRange(8))

        val result = value.toggleBulletList()

        result.text shouldBe "flour\n- sugar\neggs"
    }

    @Test
    fun secondBulletToggleRemovesTheMarkers() {
        val value = TextFieldValue("- flour\n- sugar", selection = TextRange(0, 15))

        val result = value.toggleBulletList()

        result.text shouldBe "flour\nsugar"
    }

    @Test
    fun numbersSelectedLinesSequentially() {
        val value = TextFieldValue("boil water\nadd pasta\ndrain", selection = TextRange(0, 26))

        val result = value.toggleNumberedList()

        result.text shouldBe "1. boil water\n2. add pasta\n3. drain"
    }

    @Test
    fun numberingReplacesExistingBullets() {
        val value = TextFieldValue("- boil water\n- add pasta", selection = TextRange(0, 24))

        val result = value.toggleNumberedList()

        result.text shouldBe "1. boil water\n2. add pasta"
    }

    @Test
    fun bulletingReplacesExistingNumbers() {
        val value = TextFieldValue("1. boil water\n2. add pasta", selection = TextRange(0, 26))

        val result = value.toggleBulletList()

        result.text shouldBe "- boil water\n- add pasta"
    }

    @Test
    fun blankLinesInRangeAreLeftUntouchedAndNotNumbered() {
        val value = TextFieldValue("boil water\n\ndrain", selection = TextRange(0, 17))

        val result = value.toggleNumberedList()

        result.text shouldBe "1. boil water\n\n2. drain"
    }
}
