package com.plusmobileapps.chefmate.recipe.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IngredientSectionTest {

    @Test
    fun line_ending_in_colon_is_a_header() {
        assertTrue(IngredientSection.isHeader("For the green sauce:"))
    }

    @Test
    fun header_detection_ignores_surrounding_whitespace() {
        assertTrue(IngredientSection.isHeader("  For the sauce:  "))
    }

    @Test
    fun regular_ingredient_is_not_a_header() {
        assertFalse(IngredientSection.isHeader("1 cup sour cream"))
    }

    @Test
    fun lone_colon_is_not_a_header() {
        assertFalse(IngredientSection.isHeader(":"))
    }

    @Test
    fun blank_line_is_not_a_header() {
        assertFalse(IngredientSection.isHeader("   "))
    }

    @Test
    fun header_appends_colon_when_missing() {
        assertEquals("Sauce Ingredients:", IngredientSection.header("Sauce Ingredients"))
    }

    @Test
    fun header_does_not_double_up_existing_colon() {
        assertEquals("For the sauce:", IngredientSection.header("For the sauce:"))
    }

    @Test
    fun header_trims_whitespace() {
        assertEquals("For the chicken:", IngredientSection.header("  For the chicken  "))
    }

    @Test
    fun header_of_blank_name_is_empty() {
        assertEquals("", IngredientSection.header("   "))
    }
}
