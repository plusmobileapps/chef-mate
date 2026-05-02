package com.plusmobileapps.chefmate.browser.impl

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface RecipeExtractorService {
    suspend fun extractRecipe(url: String): ExtractedRecipeData
}

@SingleIn(AppScope::class)
@Inject
@ContributesBinding(AppScope::class)
class RecipeExtractorServiceImpl(@IO private val ioContext: CoroutineContext) :
    RecipeExtractorService {

    private val httpClient = HttpClient()

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun extractRecipe(url: String): ExtractedRecipeData =
        withContext(ioContext) {
            val html =
                httpClient
                    .get(url) { header("User-Agent", "Mozilla/5.0 (compatible; ChefMate/1.0)") }
                    .bodyAsText()

            val document = Ksoup.parse(html)
            val jsonLdScripts = document.select("script[type=application/ld+json]")

            for (script in jsonLdScripts) {
                val jsonText = script.data()
                val recipeJson = findRecipeJson(jsonText) ?: continue
                return@withContext parseRecipeFromJson(recipeJson, url)
            }

            throw IllegalStateException("No recipe data found on this page")
        }

    private fun findRecipeJson(jsonText: String): JsonObject? {
        val element = json.parseToJsonElement(jsonText)
        return findRecipeInElement(element)
    }

    private fun findRecipeInElement(element: JsonElement): JsonObject? {
        return when (element) {
            is JsonObject -> {
                val type = element["@type"]
                val isRecipe =
                    when {
                        type == null -> false
                        type is JsonArray -> type.any { it.jsonPrimitive.contentOrNull == "Recipe" }
                        else -> type.jsonPrimitive.contentOrNull == "Recipe"
                    }
                if (isRecipe) {
                    element
                } else {
                    // Check @graph array
                    val graph = element["@graph"]
                    if (graph is JsonArray) {
                        graph.firstNotNullOfOrNull { findRecipeInElement(it) }
                    } else {
                        null
                    }
                }
            }
            is JsonArray -> element.firstNotNullOfOrNull { findRecipeInElement(it) }
            else -> null
        }
    }

    internal fun parseRecipeJsonText(jsonText: String, sourceUrl: String): ExtractedRecipeData? {
        val recipeJson = findRecipeJson(jsonText) ?: return null
        return parseRecipeFromJson(recipeJson, sourceUrl)
    }

    private fun String.decodeHtmlEntities(): String = Parser.unescapeEntities(this, false)

    private fun parseRecipeFromJson(obj: JsonObject, sourceUrl: String): ExtractedRecipeData {
        val title = obj["name"]?.jsonPrimitive?.contentOrNull?.decodeHtmlEntities() ?: ""
        val description = obj["description"]?.jsonPrimitive?.contentOrNull?.decodeHtmlEntities()

        val ingredients =
            obj["recipeIngredient"]?.jsonArray?.mapNotNull {
                it.jsonPrimitive.contentOrNull?.decodeHtmlEntities()
            } ?: emptyList()

        val directions = parseDirections(obj)

        val imageUrl =
            when (val img = obj["image"]) {
                is JsonArray -> img.firstOrNull()?.let { extractImageUrl(it) }
                is JsonObject -> extractImageUrl(img)
                else -> img?.jsonPrimitive?.contentOrNull
            }

        val servings = parseServings(obj)
        val prepTime = parseDuration(obj["prepTime"]?.jsonPrimitive?.contentOrNull)
        val cookTime = parseDuration(obj["cookTime"]?.jsonPrimitive?.contentOrNull)
        val totalTime = parseDuration(obj["totalTime"]?.jsonPrimitive?.contentOrNull)
        val calories = parseCalories(obj)

        return ExtractedRecipeData(
            title = title,
            description = description,
            ingredients = ingredients,
            directions = directions,
            imageUrl = imageUrl,
            sourceUrl = sourceUrl,
            servings = servings,
            prepTime = prepTime,
            cookTime = cookTime,
            totalTime = totalTime,
            calories = calories,
        )
    }

    private fun parseDirections(obj: JsonObject): List<String> {
        val instructions = obj["recipeInstructions"] ?: return emptyList()
        return when (instructions) {
            is JsonArray ->
                instructions.flatMap { element ->
                    when {
                        element is JsonObject &&
                            element["@type"]?.jsonPrimitive?.contentOrNull == "HowToStep" ->
                            listOfNotNull(
                                element["text"]?.jsonPrimitive?.contentOrNull?.decodeHtmlEntities()
                            )
                        element is JsonObject &&
                            element["@type"]?.jsonPrimitive?.contentOrNull == "HowToSection" -> {
                            val items =
                                element["itemListElement"]?.jsonArray ?: return@flatMap emptyList()
                            items.mapNotNull { item ->
                                item.jsonObject["text"]
                                    ?.jsonPrimitive
                                    ?.contentOrNull
                                    ?.decodeHtmlEntities()
                            }
                        }
                        else ->
                            listOfNotNull(element.jsonPrimitive.contentOrNull?.decodeHtmlEntities())
                    }
                }
            else -> listOfNotNull(instructions.jsonPrimitive.contentOrNull?.decodeHtmlEntities())
        }
    }

    private fun extractImageUrl(element: JsonElement): String? =
        when (element) {
            is JsonObject -> element["url"]?.jsonPrimitive?.contentOrNull
            else -> element.jsonPrimitive.contentOrNull
        }

    private fun parseServings(obj: JsonObject): Int? {
        val yield = obj["recipeYield"] ?: return null
        return when (yield) {
            is JsonArray ->
                yield
                    .firstOrNull()
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.filter { it.isDigit() }
                    ?.toIntOrNull()
            else -> yield.jsonPrimitive.contentOrNull?.filter { it.isDigit() }?.toIntOrNull()
        }
    }

    private fun parseCalories(obj: JsonObject): Int? {
        val nutrition = obj["nutrition"]?.jsonObject ?: return null
        return nutrition["calories"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.filter { it.isDigit() }
            ?.toIntOrNull()
    }

    companion object {
        fun parseDuration(iso8601: String?): Int? {
            if (iso8601 == null) return null
            var minutes = 0
            val upper = iso8601.uppercase()
            if (!upper.startsWith("PT")) return null
            val remaining = upper.removePrefix("PT")

            val hourMatch = Regex("(\\d+)H").find(remaining)
            if (hourMatch != null) minutes += hourMatch.groupValues[1].toInt() * 60

            val minMatch = Regex("(\\d+)M").find(remaining)
            if (minMatch != null) minutes += minMatch.groupValues[1].toInt()

            return if (minutes > 0) minutes else null
        }
    }
}
