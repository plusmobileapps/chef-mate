package com.plusmobileapps.chefmate.recipe.data

import kotlin.test.Test
import kotlin.test.assertEquals

class IngredientScalerTest {

    @Test
    fun factor_of_one_returns_the_original_line_unchanged() {
        // Preserves the author's exact formatting rather than re-rendering it.
        assertEquals("1/2 cup sugar", IngredientScaler.scale("1/2 cup sugar", 1.0))
    }

    @Test
    fun doubles_a_whole_number() {
        assertEquals("4 cups flour", IngredientScaler.scale("2 cups flour", 2.0))
    }

    @Test
    fun halves_a_whole_number() {
        assertEquals("1 cups flour", IngredientScaler.scale("2 cups flour", 0.5))
    }

    @Test
    fun halving_a_whole_number_yields_a_vulgar_fraction() {
        assertEquals("½ cup sugar", IngredientScaler.scale("1 cup sugar", 0.5))
    }

    @Test
    fun tripling_a_half_yields_a_mixed_number() {
        assertEquals("1½ cup milk", IngredientScaler.scale("½ cup milk", 3.0))
    }

    @Test
    fun scales_a_whole_plus_vulgar_fraction() {
        assertEquals("3 lbs beef", IngredientScaler.scale("1½ lbs beef", 2.0))
    }

    @Test
    fun scales_a_vulgar_fraction_with_a_space() {
        assertEquals("3 cups", IngredientScaler.scale("1 ½ cups", 2.0))
    }

    @Test
    fun scales_a_decimal() {
        assertEquals("3 cups", IngredientScaler.scale("1.5 cups", 2.0))
    }

    @Test
    fun scales_an_ascii_fraction() {
        assertEquals("1 cup", IngredientScaler.scale("1/2 cup", 2.0))
    }

    @Test
    fun scales_a_mixed_ascii_number() {
        assertEquals("3 cups", IngredientScaler.scale("1 1/2 cups", 2.0))
    }

    @Test
    fun halving_three_quarters_renders_a_vulgar_eighth() {
        assertEquals("⅜ cup", IngredientScaler.scale("3/4 cup", 0.5))
    }

    @Test
    fun scales_thirds_and_rounds_to_a_vulgar_fraction() {
        assertEquals("⅓ cup", IngredientScaler.scale("⅔ cup", 0.5))
    }

    @Test
    fun scales_both_ends_of_a_hyphen_range() {
        assertEquals("4-6 cloves garlic", IngredientScaler.scale("2-3 cloves garlic", 2.0))
    }

    @Test
    fun scales_both_ends_of_a_to_range() {
        assertEquals("4 to 6 cups", IngredientScaler.scale("2 to 3 cups", 2.0))
    }

    @Test
    fun leaves_a_line_with_no_leading_amount_untouched() {
        assertEquals("Salt to taste", IngredientScaler.scale("Salt to taste", 2.0))
    }

    @Test
    fun leaves_a_section_header_untouched() {
        assertEquals("For the sauce:", IngredientScaler.scale("For the sauce:", 2.0))
    }

    @Test
    fun scales_the_amount_ahead_of_inline_markdown() {
        // Callers pass the marker-stripped content, which may still contain **bold** spans.
        assertEquals("800g **spaghetti**", IngredientScaler.scale("400g **spaghetti**", 2.0))
    }

    @Test
    fun preserves_leading_whitespace() {
        assertEquals("  4 eggs", IngredientScaler.scale("  2 eggs", 2.0))
    }

    @Test
    fun halving_five_yields_a_mixed_number() {
        assertEquals("2½ cups", IngredientScaler.scale("5 cups", 0.5))
    }

    @Test
    fun falls_back_to_a_trimmed_decimal_when_no_common_fraction_is_close() {
        assertEquals("0.1 cup", IngredientScaler.scale("⅕ cup", 0.5))
    }
}
