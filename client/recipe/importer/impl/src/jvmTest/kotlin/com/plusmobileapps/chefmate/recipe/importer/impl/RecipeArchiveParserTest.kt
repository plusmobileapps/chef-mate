@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.recipe.importer.impl

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.Test

class RecipeArchiveParserTest {

    @Test
    fun When_archive_has_recipe_Then_all_fields_are_extracted() {
        val image = byteArrayOf(1, 2, 3, 4)
        val archive =
            zip(
                "Garlic Noodles.html" to garlicNoodlesHtml.encodeToByteArray(),
                "Images/garlic.jpg" to image,
            )

        val recipes = RecipeArchiveParser.parse(archive)

        recipes.size shouldBe 1
        val recipe = recipes.single()
        recipe.title shouldBe "Garlic Noodles"
        recipe.description shouldBe "Quick weeknight noodles."
        recipe.ingredients shouldContainExactly
            listOf("200g noodles", "4 cloves garlic", "2 tbsp soy sauce")
        recipe.directions shouldContainExactly
            listOf("Boil the noodles.", "Fry the garlic.", "Toss together.")
        recipe.sourceUrl shouldBe "https://example.com/garlic-noodles"
        recipe.servings shouldBe 4
        recipe.prepTime shouldBe 10
        recipe.cookTime shouldBe 15
        recipe.totalTime shouldBe 25
        recipe.calories shouldBe 520
        recipe.starRating shouldBe 5
        recipe.imageBytes?.toList() shouldBe image.toList()
        recipe.imageExtension shouldBe "jpg"
    }

    @Test
    fun When_step_numbers_prefix_directions_Then_they_are_stripped() {
        val archive = zip("r.html" to numberedStepsHtml.encodeToByteArray())

        val recipe = RecipeArchiveParser.parse(archive).single()

        recipe.directions shouldContainExactly listOf("Preheat the oven.", "Bake for 20 minutes.")
    }

    @Test
    fun When_same_recipe_listed_twice_Then_deduped_preferring_the_one_with_an_image() {
        val image = byteArrayOf(9, 8, 7)
        val archive =
            zip(
                // A table-of-contents page re-lists the recipe without an image.
                "index.html" to indexHtml.encodeToByteArray(),
                "Garlic Noodles.html" to garlicNoodlesHtml.encodeToByteArray(),
                "Images/garlic.jpg" to image,
            )

        val recipes = RecipeArchiveParser.parse(archive)

        recipes.size shouldBe 1
        recipes.single().imageBytes?.toList() shouldBe image.toList()
    }

    @Test
    fun When_macos_metadata_present_Then_it_is_ignored() {
        val archive =
            zip(
                "__MACOSX/._Garlic Noodles.html" to byteArrayOf(0, 0, 0),
                "Garlic Noodles.html" to garlicNoodlesHtml.encodeToByteArray(),
            )

        RecipeArchiveParser.parse(archive).size shouldBe 1
    }

    @Test
    fun When_entry_has_no_recipe_microdata_Then_it_yields_no_recipes() {
        val archive =
            zip("plain.html" to "<html><body><p>Just a page.</p></body></html>".encodeToByteArray())

        RecipeArchiveParser.parse(archive) shouldBe emptyList()
    }

    private fun zip(vararg entries: Pair<String, ByteArray>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            for ((name, bytes) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    private val garlicNoodlesHtml =
        """
        <html><body>
          <div itemscope itemtype="http://schema.org/Recipe">
            <h1 itemprop="name">Garlic Noodles</h1>
            <p itemprop="description">Quick weeknight noodles.</p>
            <a itemprop="url" href="https://example.com/garlic-noodles">source</a>
            <span itemprop="recipeYield">Serves 4</span>
            <time itemprop="prepTime">10 minutes</time>
            <time itemprop="cookTime">15 minutes</time>
            <time itemprop="totalTime">25 minutes</time>
            <div itemprop="nutrition">Calories: 520</div>
            <span itemprop="aggregateRating" value="5"></span>
            <img itemprop="image" src="Images/garlic.jpg" />
            <ul>
              <li itemprop="recipeIngredient">200g noodles</li>
              <li itemprop="recipeIngredient">4 cloves garlic</li>
              <li itemprop="recipeIngredient">2 tbsp soy sauce</li>
            </ul>
            <div itemprop="recipeInstructions">
              <p>Boil the noodles.</p>
              <p>Fry the garlic.</p>
              <p>Toss together.</p>
            </div>
          </div>
        </body></html>
        """
            .trimIndent()

    private val numberedStepsHtml =
        """
        <html><body>
          <div itemscope itemtype="http://schema.org/Recipe">
            <h1 itemprop="name">Bread</h1>
            <div itemprop="recipeInstructions">
              <p>1. Preheat the oven.</p>
              <p>2) Bake for 20 minutes.</p>
            </div>
          </div>
        </body></html>
        """
            .trimIndent()

    private val indexHtml =
        """
        <html><body>
          <div itemscope itemtype="http://schema.org/Recipe">
            <h1 itemprop="name">Garlic Noodles</h1>
          </div>
        </body></html>
        """
            .trimIndent()
}
