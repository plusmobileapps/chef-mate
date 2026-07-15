package com.plusmobileapps.chefmate.recipe.core.detail

import com.plusmobileapps.chefmate.ui.text.LineMarker
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DirectionStepsTest {

    @Test
    fun plain_steps_have_no_marker() {
        val steps = directionSteps("Boil water\nAdd pasta\nDrain")
        steps.map { it.marker } shouldBe listOf(LineMarker.None, LineMarker.None, LineMarker.None)
        steps.map { it.isHeader } shouldBe listOf(false, false, false)
        steps.map { it.content } shouldBe listOf("Boil water", "Add pasta", "Drain")
    }

    @Test
    fun keeps_manual_leading_enumerators_as_the_displayed_number() {
        val steps = directionSteps("1. Boil water\n2) Add pasta\n3.  Drain")
        steps.map { it.content } shouldBe listOf("Boil water", "Add pasta", "Drain")
        steps.map { it.marker } shouldBe
            listOf(LineMarker.Ordered(1), LineMarker.Ordered(2), LineMarker.Ordered(3))
    }

    @Test
    fun preserves_the_authors_own_numbering() {
        // The author's numbers are shown verbatim — not renumbered sequentially.
        val steps = directionSteps("1. Preheat oven\n5. Bake\n10. Cool")
        steps.map { it.marker } shouldBe
            listOf(LineMarker.Ordered(1), LineMarker.Ordered(5), LineMarker.Ordered(10))
    }

    @Test
    fun recognizes_bulleted_steps() {
        val steps = directionSteps("- Chop onions\n- Sauté")
        steps.map { it.marker } shouldBe listOf(LineMarker.Bullet, LineMarker.Bullet)
        steps.map { it.content } shouldBe listOf("Chop onions", "Sauté")
    }

    @Test
    fun headers_are_flagged_and_never_marked() {
        val steps =
            directionSteps(
                """
                Make the sauce:
                Heat oil
                Add garlic
                Assemble:
                Combine everything
                """
                    .trimIndent()
            )
        steps.map { it.isHeader } shouldBe listOf(true, false, false, true, false)
        steps.map { it.marker } shouldBe
            listOf(
                LineMarker.None,
                LineMarker.None,
                LineMarker.None,
                LineMarker.None,
                LineMarker.None,
            )
        steps[0].content shouldBe "Make the sauce:"
        steps[3].content shouldBe "Assemble:"
    }

    @Test
    fun blank_lines_are_dropped() {
        val steps = directionSteps("Boil water\n\n\nAdd pasta")
        steps.map { it.content } shouldBe listOf("Boil water", "Add pasta")
    }

    @Test
    fun leaves_inline_markdown_for_the_renderer() {
        val steps = directionSteps("Cook until **golden**")
        steps.single().content shouldBe "Cook until **golden**"
        steps.single().marker shouldBe LineMarker.None
    }
}
