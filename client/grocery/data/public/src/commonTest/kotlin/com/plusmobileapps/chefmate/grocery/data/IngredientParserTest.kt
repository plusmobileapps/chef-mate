@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.grocery.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IngredientParserTest {

    @Test
    fun parse_quantity_with_cups() {
        val result = IngredientParser.parse("2 cups flour")
        assertEquals("flour", result.name)
        assertEquals("2 cups", result.quantity)
        assertEquals(GroceryCategory.BAKING, result.category)
    }

    @Test
    fun parse_fractional_quantity() {
        val result = IngredientParser.parse("1/2 tsp salt")
        assertEquals("salt", result.name)
        assertEquals("1/2 tsp", result.quantity)
        assertEquals(GroceryCategory.SPICES, result.category)
    }

    @Test
    fun parse_no_quantity() {
        val result = IngredientParser.parse("milk")
        assertEquals("milk", result.name)
        assertNull(result.quantity)
        assertEquals(GroceryCategory.DAIRY, result.category)
    }

    @Test
    fun parse_large_quantity_modifier() {
        val result = IngredientParser.parse("3 large eggs")
        assertEquals("eggs", result.name)
        assertEquals("3 large", result.quantity)
        assertEquals(GroceryCategory.DAIRY, result.category)
    }

    @Test
    fun parse_canned_item() {
        val result = IngredientParser.parse("1 can tomato sauce")
        assertEquals("tomato sauce", result.name)
        assertEquals("1 can", result.quantity)
        assertEquals(GroceryCategory.CANNED_GOODS, result.category)
    }

    @Test
    fun parse_meat_item() {
        val result = IngredientParser.parse("1 lb chicken breast")
        assertEquals("chicken breast", result.name)
        assertEquals("1 lb", result.quantity)
        assertEquals(GroceryCategory.MEAT, result.category)
    }

    @Test
    fun parse_produce_no_quantity() {
        val result = IngredientParser.parse("avocado")
        assertEquals("avocado", result.name)
        assertNull(result.quantity)
        assertEquals(GroceryCategory.PRODUCE, result.category)
    }

    @Test
    fun parse_empty_string() {
        val result = IngredientParser.parse("")
        assertEquals("", result.name)
        assertNull(result.quantity)
        assertEquals(GroceryCategory.OTHER, result.category)
    }

    @Test
    fun parse_blank_string() {
        val result = IngredientParser.parse("   ")
        assertEquals("", result.name)
        assertNull(result.quantity)
        assertEquals(GroceryCategory.OTHER, result.category)
    }

    @Test
    fun parse_unknown_item_categorized_as_other() {
        val result = IngredientParser.parse("tofu")
        assertEquals("tofu", result.name)
        assertNull(result.quantity)
        assertEquals(GroceryCategory.OTHER, result.category)
    }

    @Test
    fun parse_condiment() {
        val result = IngredientParser.parse("2 tbsp soy sauce")
        assertEquals("soy sauce", result.name)
        assertEquals("2 tbsp", result.quantity)
        assertEquals(GroceryCategory.CONDIMENTS, result.category)
    }

    @Test
    fun parse_spice_with_tablespoon() {
        val result = IngredientParser.parse("1 tablespoon cinnamon")
        assertEquals("cinnamon", result.name)
        assertEquals("1 tablespoon", result.quantity)
        assertEquals(GroceryCategory.SPICES, result.category)
    }

    @Test
    fun parse_grains() {
        val result = IngredientParser.parse("2 cups rice")
        assertEquals("rice", result.name)
        assertEquals("2 cups", result.quantity)
        assertEquals(GroceryCategory.GRAINS, result.category)
    }

    @Test
    fun parse_bakery_item() {
        val result = IngredientParser.parse("bread")
        assertEquals("bread", result.name)
        assertNull(result.quantity)
        assertEquals(GroceryCategory.BAKERY, result.category)
    }

    @Test
    fun parse_ounces() {
        val result = IngredientParser.parse("8 oz cream cheese")
        assertEquals("cream cheese", result.name)
        assertEquals("8 oz", result.quantity)
        assertEquals(GroceryCategory.DAIRY, result.category)
    }

    @Test
    fun parse_preserves_leading_whitespace_trimmed() {
        val result = IngredientParser.parse("  2 cups sugar  ")
        assertEquals("sugar", result.name)
        assertEquals("2 cups", result.quantity)
        assertEquals(GroceryCategory.BAKING, result.category)
    }

    @Test
    fun parse_sage_with_parenthetical_teaspoons() {
        val result =
            IngredientParser.parse(
                "1/4 cup minced fresh sage leaves (or 2 teaspoons dried sage leaves)"
            )
        assertEquals("minced fresh sage leaves (or 2 teaspoons dried sage leaves)", result.name)
        assertEquals("1/4 cup", result.quantity)
        assertEquals(GroceryCategory.SPICES, result.category)
    }

    @Test
    fun parse_black_pepper_categorized_as_spice() {
        val result = IngredientParser.parse("1 tsp black pepper")
        assertEquals("black pepper", result.name)
        assertEquals("1 tsp", result.quantity)
        assertEquals(GroceryCategory.SPICES, result.category)
    }

    @Test
    fun parse_tomato_sauce_categorized_as_canned_goods() {
        val result = IngredientParser.parse("1 can tomato sauce")
        assertEquals("tomato sauce", result.name)
        assertEquals("1 can", result.quantity)
        assertEquals(GroceryCategory.CANNED_GOODS, result.category)
    }

    @Test
    fun parse_coconut_milk_categorized_as_canned_goods() {
        val result = IngredientParser.parse("1 can coconut milk")
        assertEquals("coconut milk", result.name)
        assertEquals("1 can", result.quantity)
        assertEquals(GroceryCategory.CANNED_GOODS, result.category)
    }

    @Test
    fun parse_chicken_broth_categorized_as_canned_goods() {
        val result = IngredientParser.parse("2 cups chicken broth")
        assertEquals("chicken broth", result.name)
        assertEquals("2 cups", result.quantity)
        assertEquals(GroceryCategory.CANNED_GOODS, result.category)
    }

    @Test
    fun parse_beef_stock_categorized_as_canned_goods() {
        val result = IngredientParser.parse("1 qt beef stock")
        assertEquals("beef stock", result.name)
        assertEquals("1 qt", result.quantity)
        assertEquals(GroceryCategory.CANNED_GOODS, result.category)
    }
}
