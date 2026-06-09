package com.plusmobileapps.chefmate.recipe.core.impl.text

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InlineMarkdownTest {

    @Test
    fun plainTextHasNoSpans() {
        val result = "2 cups flour".toInlineMarkdownAnnotatedString()
        result.text shouldBe "2 cups flour"
        result.spanStyles.size shouldBe 0
    }

    @Test
    fun boldMarkersAreStrippedAndStyled() {
        val result = "**preheat** the oven".toInlineMarkdownAnnotatedString()
        result.text shouldBe "preheat the oven"
        result.spanStyles.size shouldBe 1
        val span = result.spanStyles.single()
        span.start shouldBe 0
        span.end shouldBe 7
        span.item.fontWeight shouldBe FontWeight.Bold
    }

    @Test
    fun italicMarkersAreStrippedAndStyled() {
        val result = "stir _gently_".toInlineMarkdownAnnotatedString()
        result.text shouldBe "stir gently"
        val span = result.spanStyles.single()
        span.start shouldBe 5
        span.end shouldBe 11
        span.item.fontStyle shouldBe FontStyle.Italic
    }

    @Test
    fun boldCanNestItalic() {
        val result = "**bold _and italic_**".toInlineMarkdownAnnotatedString()
        result.text shouldBe "bold and italic"
        val bold = result.spanStyles.first { it.item.fontWeight == FontWeight.Bold }
        bold.start shouldBe 0
        bold.end shouldBe 15
        val italic = result.spanStyles.first { it.item.fontStyle == FontStyle.Italic }
        italic.start shouldBe 5
        italic.end shouldBe 15
    }

    @Test
    fun unmatchedMarkerIsLiteral() {
        val result = "**not closed".toInlineMarkdownAnnotatedString()
        result.text shouldBe "**not closed"
        result.spanStyles.size shouldBe 0
    }

    @Test
    fun strayItalicMarkerIsLiteral() {
        val result = "salt _ pepper".toInlineMarkdownAnnotatedString()
        result.text shouldBe "salt _ pepper"
        result.spanStyles.size shouldBe 0
    }
}
