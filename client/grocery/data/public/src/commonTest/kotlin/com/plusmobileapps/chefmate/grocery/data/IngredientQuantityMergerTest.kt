@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.grocery.data

import kotlin.test.Test
import kotlin.test.assertEquals

class IngredientQuantityMergerTest {

    @Test
    fun merge_sums_matching_units() {
        val result = IngredientQuantityMerger.merge("2 cups flour", "1 cup flour")
        assertEquals("3 cups flour", result)
    }

    @Test
    fun merge_sums_bare_counts_with_no_unit() {
        val result = IngredientQuantityMerger.merge("2 eggs", "3 eggs")
        assertEquals("5 eggs", result)
    }

    @Test
    fun merge_sums_fractional_amounts() {
        val result = IngredientQuantityMerger.merge("1/2 cup sugar", "1/2 cup sugar")
        assertEquals("1 cup sugar", result)
    }

    @Test
    fun merge_sums_unicode_fraction_amounts() {
        val result = IngredientQuantityMerger.merge("1½ cups milk", "½ cup milk")
        assertEquals("2 cups milk", result)
    }

    @Test
    fun merge_normalizes_unit_synonyms_before_summing() {
        val result = IngredientQuantityMerger.merge("1 tablespoon oil", "2 tbsp oil")
        assertEquals("3 tbsp oil", result)
    }

    @Test
    fun merge_concatenates_incompatible_units() {
        val result = IngredientQuantityMerger.merge("2 cups flour", "3 oz flour")
        assertEquals("2 cups + 3 oz flour", result)
    }

    @Test
    fun merge_keeps_known_quantity_when_the_other_has_none() {
        val existingKnown = IngredientQuantityMerger.merge("2 tsp salt", "salt")
        val newKnown = IngredientQuantityMerger.merge("salt", "2 tsp salt")

        assertEquals("2 tsp salt", existingKnown)
        assertEquals("2 tsp salt", newKnown)
    }

    @Test
    fun merge_returns_bare_name_when_neither_has_a_quantity() {
        val result = IngredientQuantityMerger.merge("salt", "salt")
        assertEquals("salt", result)
    }

    @Test
    fun merge_preserves_existing_display_casing() {
        val result = IngredientQuantityMerger.merge("2 cups Flour", "1 cup flour")
        assertEquals("3 cups Flour", result)
    }
}
