package com.plusmobileapps.chefmate.recipe.core.detail

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DirectionStepsTest {

    @Test
    fun plain_steps_are_not_auto_numbered() {
        val steps = directionSteps("Boil water\nAdd pasta\nDrain")
        steps.map { it.number } shouldBe listOf(null, null, null)
        steps.map { it.isHeader } shouldBe listOf(false, false, false)
        steps.map { it.text } shouldBe listOf("Boil water", "Add pasta", "Drain")
    }

    @Test
    fun keeps_manual_leading_enumerators_as_the_displayed_number() {
        val steps = directionSteps("1. Boil water\n2) Add pasta\n3.  Drain")
        steps.map { it.text } shouldBe listOf("Boil water", "Add pasta", "Drain")
        steps.map { it.number } shouldBe listOf(1, 2, 3)
    }

    @Test
    fun preserves_the_authors_own_numbering() {
        // The author's numbers are shown verbatim — not renumbered sequentially.
        val steps = directionSteps("1. Preheat oven\n5. Bake\n10. Cool")
        steps.map { it.number } shouldBe listOf(1, 5, 10)
    }

    @Test
    fun headers_are_flagged_and_never_numbered() {
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
        steps.map { it.number } shouldBe listOf(null, null, null, null, null)
        steps[0].text shouldBe "Make the sauce:"
        steps[3].text shouldBe "Assemble:"
    }

    @Test
    fun blank_lines_are_dropped() {
        val steps = directionSteps("Boil water\n\n\nAdd pasta")
        steps.map { it.text } shouldBe listOf("Boil water", "Add pasta")
    }

    @Test
    fun leaves_inline_markdown_for_the_renderer() {
        val steps = directionSteps("Cook until **golden**")
        steps.single().text shouldBe "Cook until **golden**"
        steps.single().number shouldBe null
    }
}
