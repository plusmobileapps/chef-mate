package com.plusmobileapps.chefmate.recipe.core.detail

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DirectionStepsTest {

    @Test
    fun numbers_each_step_sequentially() {
        val steps = directionSteps("Boil water\nAdd pasta\nDrain")
        steps.map { it.number } shouldBe listOf(1, 2, 3)
        steps.map { it.text } shouldBe listOf("Boil water", "Add pasta", "Drain")
    }

    @Test
    fun strips_manual_leading_enumerators() {
        val steps = directionSteps("1. Boil water\n2) Add pasta\n3.  Drain")
        steps.map { it.text } shouldBe listOf("Boil water", "Add pasta", "Drain")
        steps.map { it.number } shouldBe listOf(1, 2, 3)
    }

    @Test
    fun headers_are_unnumbered_and_reset_the_counter() {
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
        steps.map { it.number } shouldBe listOf(null, 1, 2, null, 1)
        steps[0].text shouldBe "Make the sauce:"
        steps[3].text shouldBe "Assemble:"
    }

    @Test
    fun blank_lines_are_dropped() {
        val steps = directionSteps("Boil water\n\n\nAdd pasta")
        steps.map { it.number } shouldBe listOf(1, 2)
    }

    @Test
    fun leaves_inline_markdown_for_the_renderer() {
        val steps = directionSteps("Cook until **golden**")
        steps.single().text shouldBe "Cook until **golden**"
    }
}
