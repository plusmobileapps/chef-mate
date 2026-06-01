@file:Suppress("FunctionName")
@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.exporter.impl

import com.plusmobileapps.chefmate.recipe.data.Recipe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.string.shouldContain
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class RecipeArchiveBuilderTest {

    @Test
    fun build_produces_index_and_one_html_per_recipe() {
        val recipes =
            listOf(
                recipe(title = "Garlic Noodles"),
                recipe(title = "Lentil Curry"),
                recipe(title = "Meatloaf"),
            )

        val archive = RecipeArchiveBuilder.build(recipes)
        val entries = readEntries(archive)

        entries.keys shouldContain "3 recipes/index.html"
        entries.keys shouldContain "3 recipes/Recipes/Garlic Noodles.html"
        entries.keys shouldContain "3 recipes/Recipes/Lentil Curry.html"
        entries.keys shouldContain "3 recipes/Recipes/Meatloaf.html"
    }

    @Test
    fun build_writes_schema_microdata_for_ingredients_and_directions() {
        val recipe =
            recipe(
                title = "Garlic Noodles",
                ingredients = "garlic\nnoodles",
                directions = "Boil noodles.\nToss with garlic.",
            )

        val archive = RecipeArchiveBuilder.build(listOf(recipe))
        val html = readEntries(archive)["1 recipe/Recipes/Garlic Noodles.html"]!!.decodeToString()

        html shouldContain "itemtype=\"http://schema.org/Recipe\""
        html shouldContain "itemprop=\"name\""
        html shouldContain ">Garlic Noodles<"
        html shouldContain "itemprop=\"recipeIngredient\""
        html shouldContain ">garlic<"
        html shouldContain ">noodles<"
        html shouldContain "itemprop=\"recipeInstructions\""
        html shouldContain ">Boil noodles.<"
        html shouldContain ">Toss with garlic.<"
    }

    @Test
    fun build_disambiguates_duplicate_titles() {
        val recipes = listOf(recipe(title = "Pizza"), recipe(title = "Pizza"))

        val archive = RecipeArchiveBuilder.build(recipes)
        val entries = readEntries(archive)
        entries.keys shouldContain "2 recipes/Recipes/Pizza.html"
        entries.keys shouldContain "2 recipes/Recipes/Pizza (2).html"
    }

    @Test
    fun build_sanitizes_titles_with_forbidden_characters() {
        val recipes = listOf(recipe(title = "Pizza/Pasta?"))

        val archive = RecipeArchiveBuilder.build(recipes)
        readEntries(archive).keys shouldContain "1 recipe/Recipes/Pizza Pasta.html"
    }

    @Test
    fun emitted_archive_is_importer_compatible() {
        // The on-the-wire encoding is what the importer reads — round-trip the bytes through
        // the standard JVM ZipInputStream and re-parse to make sure structure is intact.
        val recipe =
            recipe(
                title = "Marcella Hazan Bolognese",
                ingredients = "olive oil\ntomatoes",
                directions = "Cook.\nServe.",
                imageUrl = "https://cdn.example.com/bolognese.jpg",
                sourceUrl = "https://abeautifulplate.com/marcella-hazan-bolognese/",
            )

        val archive = RecipeArchiveBuilder.build(listOf(recipe))
        val html =
            readEntries(archive)["1 recipe/Recipes/Marcella Hazan Bolognese.html"]!!
                .decodeToString()

        html shouldContain "src=\"https://cdn.example.com/bolognese.jpg\""
        html shouldContain "href=\"https://abeautifulplate.com/marcella-hazan-bolognese/\""
        html shouldContain ">abeautifulplate.com<"
    }

    private fun readEntries(archive: ByteArray): Map<String, ByteArray> {
        val result = LinkedHashMap<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(archive)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.isDirectory) continue
                result[entry.name] = zip.readBytes()
                zip.closeEntry()
            }
        }
        return result
    }

    private fun recipe(
        title: String,
        ingredients: String = "salt",
        directions: String = "Cook.",
        imageUrl: String? = null,
        sourceUrl: String? = null,
    ): Recipe =
        Recipe.Empty.copy(
            id = title.hashCode().toLong(),
            title = title,
            ingredients = ingredients,
            directions = directions,
            imageUrl = imageUrl,
            sourceUrl = sourceUrl,
            createdAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST,
        )
}
