@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.browser.impl

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers

class RecipeExtractorServiceTest {

    private val service = RecipeExtractorServiceImpl(Dispatchers.Default)

    // region parseDuration

    @Test
    fun parseDuration_null_returns_null() {
        RecipeExtractorServiceImpl.parseDuration(null) shouldBe null
    }

    @Test
    fun parseDuration_hours_only() {
        RecipeExtractorServiceImpl.parseDuration("PT2H") shouldBe 120
    }

    @Test
    fun parseDuration_minutes_only() {
        RecipeExtractorServiceImpl.parseDuration("PT30M") shouldBe 30
    }

    @Test
    fun parseDuration_hours_and_minutes() {
        RecipeExtractorServiceImpl.parseDuration("PT1H15M") shouldBe 75
    }

    @Test
    fun parseDuration_zero_minutes_returns_null() {
        RecipeExtractorServiceImpl.parseDuration("PT0M") shouldBe null
    }

    @Test
    fun parseDuration_invalid_format_returns_null() {
        RecipeExtractorServiceImpl.parseDuration("30 minutes") shouldBe null
    }

    // endregion

    // region HTML entity decoding

    @Test
    fun When_title_has_html_entity_apostrophe_Then_decoded() {
        val json = recipeJson(name = "The World&#39;s Easiest Cranberry Sauce")
        val recipe = service.parseRecipeJsonText(json, SOURCE_URL)
        recipe?.title shouldBe "The World's Easiest Cranberry Sauce"
    }

    @Test
    fun When_title_has_amp_entity_Then_decoded() {
        val json = recipeJson(name = "Mac &amp; Cheese")
        val recipe = service.parseRecipeJsonText(json, SOURCE_URL)
        recipe?.title shouldBe "Mac & Cheese"
    }

    @Test
    fun When_description_has_html_entity_Then_decoded() {
        val json = recipeJson(description = "It&#39;s the best recipe")
        val recipe = service.parseRecipeJsonText(json, SOURCE_URL)
        recipe?.description shouldBe "It's the best recipe"
    }

    @Test
    fun When_ingredient_has_html_entity_Then_decoded() {
        val json = recipeJson(ingredients = listOf("1 cup chef&#39;s choice broth"))
        val recipe = service.parseRecipeJsonText(json, SOURCE_URL)
        recipe?.ingredients shouldBe listOf("1 cup chef's choice broth")
    }

    @Test
    fun When_HowToStep_direction_has_html_entity_Then_decoded() {
        val json =
            recipeJson(
                instructions = """[{"@type":"HowToStep","text":"Don&#39;t overcook the sauce"}]"""
            )
        val recipe = service.parseRecipeJsonText(json, SOURCE_URL)
        recipe?.directions shouldBe listOf("Don't overcook the sauce")
    }

    @Test
    fun When_HowToSection_direction_has_html_entity_Then_decoded() {
        val json =
            recipeJson(
                instructions =
                    """[{"@type":"HowToSection","itemListElement":[{"@type":"HowToStep","text":"It&#39;s step one"}]}]"""
            )
        val recipe = service.parseRecipeJsonText(json, SOURCE_URL)
        recipe?.directions shouldBe listOf("It's step one")
    }

    @Test
    fun When_plain_string_direction_has_html_entity_Then_decoded() {
        val json = recipeJson(instructions = """["Stir &amp; serve"]""")
        val recipe = service.parseRecipeJsonText(json, SOURCE_URL)
        recipe?.directions shouldBe listOf("Stir & serve")
    }

    @Test
    fun When_recipe_json_has_no_recipe_type_Then_returns_null() {
        val json = """{"@type":"Article","name":"Not a recipe"}"""
        service.parseRecipeJsonText(json, SOURCE_URL) shouldBe null
    }

    @Test
    fun When_recipe_is_nested_in_graph_Then_parsed() {
        val json =
            """{"@graph":[{"@type":"Recipe","name":"Soup &amp; Salad","recipeIngredient":[],"recipeInstructions":[]}]}"""
        val recipe = service.parseRecipeJsonText(json, SOURCE_URL)
        recipe?.title shouldBe "Soup & Salad"
    }

    // endregion

    // region ingredient sections

    @Test
    fun When_ingredients_have_named_groups_Then_headers_are_extracted() {
        val html =
            wprmIngredients(
                group(
                    name = "For the chicken",
                    ingredients = listOf(amountUnitName("1", "lb", "chicken thighs")),
                ),
                group(
                    name = "For the green sauce",
                    ingredients = listOf(amountUnitName("½", "cup", "sour cream")),
                ),
            )
        service.parseIngredientSections(html) shouldBe
            listOf(
                "For the chicken:",
                "1 lb chicken thighs",
                "For the green sauce:",
                "½ cup sour cream",
            )
    }

    @Test
    fun When_first_group_is_unnamed_Then_only_named_section_gets_a_header() {
        val html =
            wprmIngredients(
                group(
                    name = "",
                    ingredients = listOf(amountUnitName("2", "Tablespoons", "avocado oil")),
                ),
                group(
                    name = "Sauce Ingredients",
                    ingredients = listOf(amountUnitName("3", "Tablespoons", "soy sauce")),
                ),
            )
        service.parseIngredientSections(html) shouldBe
            listOf("2 Tablespoons avocado oil", "Sauce Ingredients:", "3 Tablespoons soy sauce")
    }

    @Test
    fun When_no_group_is_named_Then_returns_null_to_fall_back_to_json_ld() {
        val html =
            wprmIngredients(
                group(name = "", ingredients = listOf(amountUnitName("1", "cup", "flour")))
            )
        service.parseIngredientSections(html) shouldBe null
    }

    @Test
    fun When_markup_is_absent_Then_returns_null() {
        service.parseIngredientSections("<html><body><p>no recipe</p></body></html>") shouldBe null
    }

    @Test
    fun When_ingredient_has_notes_Then_appended_after_comma() {
        val html =
            wprmIngredients(
                group(
                    name = "Sauce",
                    ingredients =
                        listOf(
                            """<span class="wprm-recipe-ingredient-amount">1</span>""" +
                                """<span class="wprm-recipe-ingredient-name">tofu</span>""" +
                                """<span class="wprm-recipe-ingredient-notes">16 oz</span>"""
                        ),
                )
            )
        service.parseIngredientSections(html) shouldBe listOf("Sauce:", "1 tofu, 16 oz")
    }

    private fun amountUnitName(amount: String, unit: String, name: String): String =
        """<span class="wprm-recipe-ingredient-amount">$amount</span>""" +
            """<span class="wprm-recipe-ingredient-unit">$unit</span>""" +
            """<span class="wprm-recipe-ingredient-name">$name</span>"""

    private fun group(name: String, ingredients: List<String>): String {
        val heading =
            if (name.isEmpty()) {
                ""
            } else {
                """<h4 class="wprm-recipe-group-name wprm-recipe-ingredient-group-name">$name</h4>"""
            }
        val items =
            ingredients.joinToString("") { """<li class="wprm-recipe-ingredient">$it</li>""" }
        return """<div class="wprm-recipe-ingredient-group">$heading<ul class="wprm-recipe-ingredients">$items</ul></div>"""
    }

    private fun wprmIngredients(vararg groups: String): String =
        """<html><body>${groups.joinToString("")}</body></html>"""

    // endregion

    private fun recipeJson(
        name: String = "Test Recipe",
        description: String? = null,
        ingredients: List<String> = emptyList(),
        instructions: String = "[]",
    ): String {
        val descriptionField = if (description != null) """"description":"$description",""" else ""
        val ingredientsJson = ingredients.joinToString(",") { "\"$it\"" }
        return """{"@type":"Recipe","name":"$name",$descriptionField"recipeIngredient":[$ingredientsJson],"recipeInstructions":$instructions}"""
    }

    companion object {
        private const val SOURCE_URL = "https://example.com/recipe"
    }
}
