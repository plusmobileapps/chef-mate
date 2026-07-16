package com.plusmobileapps.chefmate.ui.text

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ListMarkersTest {

    @Test
    fun parses_a_dash_bullet() {
        parseListLine("- flour") shouldBe ListLine(LineMarker.Bullet, "flour")
    }

    @Test
    fun parses_an_asterisk_bullet() {
        parseListLine("* flour") shouldBe ListLine(LineMarker.Bullet, "flour")
    }

    @Test
    fun parses_ordered_enumerators_keeping_the_authors_number() {
        parseListLine("1. Boil water") shouldBe ListLine(LineMarker.Ordered(1), "Boil water")
        parseListLine("7) Bake") shouldBe ListLine(LineMarker.Ordered(7), "Bake")
    }

    @Test
    fun a_plain_line_has_no_marker_and_is_unchanged() {
        parseListLine("Boil water") shouldBe ListLine(LineMarker.None, "Boil water")
    }

    @Test
    fun does_not_treat_a_bold_marker_as_a_bullet() {
        // "**bold**" starts with '*' but the next char isn't whitespace, so it's not a bullet.
        parseListLine("**bold** step") shouldBe ListLine(LineMarker.None, "**bold** step")
    }

    @Test
    fun does_not_treat_a_hyphenated_quantity_as_a_bullet() {
        parseListLine("-1/2 cup sugar") shouldBe ListLine(LineMarker.None, "-1/2 cup sugar")
    }
}
