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
    fun merge_bumps_a_size_descriptor_count_when_the_other_is_bare() {
        // "large" describes a countable item, not a measurement, so a bare re-add of
        // "red bell pepper (sliced)" against "2 large red bell pepper (sliced)" means three of
        // them — not a silent no-op.
        val existingLarge =
            IngredientQuantityMerger.merge(
                "2 large red bell pepper (sliced)",
                "red bell pepper (sliced)",
            )
        val newLarge =
            IngredientQuantityMerger.merge(
                "red bell pepper (sliced)",
                "2 large red bell pepper (sliced)",
            )

        assertEquals("3 large red bell pepper (sliced)", existingLarge)
        assertEquals("3 large red bell pepper (sliced)", newLarge)
    }

    @Test
    fun merge_bumps_a_countable_unit_when_the_other_is_bare() {
        val result = IngredientQuantityMerger.merge("8 cloves garlic", "garlic")
        assertEquals("9 cloves garlic", result)
    }

    @Test
    fun merge_keeps_a_measurement_quantity_when_the_other_has_none() {
        // A dimensionless "+1" can't be summed onto a volume/weight measurement, so the measured
        // quantity is kept rather than mangled.
        val existingKnown = IngredientQuantityMerger.merge("2 tsp salt", "salt")
        val newKnown = IngredientQuantityMerger.merge("salt", "2 tsp salt")
        val cups = IngredientQuantityMerger.merge("2 cups flour", "flour")

        assertEquals("2 tsp salt", existingKnown)
        assertEquals("2 tsp salt", newKnown)
        assertEquals("2 cups flour", cups)
    }

    @Test
    fun merge_bumps_count_when_neither_has_a_quantity() {
        // A bare item is an implicit count of 1, so re-adding it bumps a plain count instead of
        // silently no-op'ing (issue #483 follow-up: adding a second "red bell pepper").
        val result = IngredientQuantityMerger.merge("red bell pepper", "red bell pepper")
        assertEquals("2 red bell pepper", result)
    }

    @Test
    fun merge_bumps_count_when_only_existing_has_a_bare_number() {
        // Recipe line "1 red bell pepper" merged with a manual bare "red bell pepper" should
        // still count as two, not stay at one.
        val existingNumbered =
            IngredientQuantityMerger.merge("1 red bell pepper", "red bell pepper")
        val newNumbered = IngredientQuantityMerger.merge("red bell pepper", "1 red bell pepper")

        assertEquals("2 red bell pepper", existingNumbered)
        assertEquals("2 red bell pepper", newNumbered)
    }

    @Test
    fun merge_preserves_existing_display_casing() {
        val result = IngredientQuantityMerger.merge("2 cups Flour", "1 cup flour")
        assertEquals("3 cups Flour", result)
    }
}
