package com.plusmobileapps.chefmate.browser.impl

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.parser.Parser
import com.plusmobileapps.chefmate.ChefMateUrls
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.recipe.data.IngredientSection
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
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
class RecipeExtractorServiceImpl(
    @IO private val ioContext: CoroutineContext,
    private val recipeRepository: RecipeRepository,
) : RecipeExtractorService {

    private val httpClient = HttpClient()

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun extractRecipe(url: String): ExtractedRecipeData =
        withContext(ioContext) {
            // Our own share links render their recipe content client-side after load (fetched from
            // Supabase), so there's no server-rendered markup for the scraper below to parse —
            // fetch the recipe directly instead of scraping the page.
            ChefMateUrls.recipeShareUrlRemoteId(url)?.let { remoteId ->
                return@withContext recipeRepository
                    .fetchPublicRecipe(remoteId)
                    .getOrElse { throw IllegalStateException("No recipe data found on this page") }
                    .toExtractedRecipeData(url)
            }

            val response = httpClient.get(url) { header("User-Agent", USER_AGENT) }
            if (!response.status.isSuccess()) {
                throw IllegalStateException(
                    "Could not load the page (HTTP ${response.status.value})"
                )
            }
            val html = response.bodyAsText()

            val document = Ksoup.parse(html)
            val jsonLdScripts = document.select("script[type=application/ld+json]")

            for (script in jsonLdScripts) {
                val jsonText = script.data()
                val recipeJson = findRecipeJson(jsonText) ?: continue
                val data = parseRecipeFromJson(recipeJson, url)
                // schema.org flattens grouped ingredients, so recover any sub-section headers
                // (e.g. "For the sauce:") from the page markup when present.
                val grouped = parseIngredientSections(document)
                return@withContext if (grouped != null) data.copy(ingredients = grouped) else data
            }

            throw IllegalStateException("No recipe data found on this page")
        }

    private fun Recipe.toExtractedRecipeData(sourceUrl: String): ExtractedRecipeData =
        ExtractedRecipeData(
            title = title,
            description = description,
            ingredients = ingredients.splitLines(),
            directions = directions.splitLines(),
            imageUrl = imageUrl,
            sourceUrl = sourceUrl,
            servings = servings,
            prepTime = prepTime,
            cookTime = cookTime,
            totalTime = totalTime,
            calories = calories,
        )

    private fun String.splitLines(): List<String> = split("\n").filter { it.isNotBlank() }

    /**
     * Recovers grouped ingredients (with their sub-section headers) from WP Recipe Maker markup,
     * the most common WordPress recipe plugin. Returns `null` when the markup is absent or has no
     * named groups, so the caller can fall back to the flat JSON-LD ingredient list.
     */
    internal fun parseIngredientSections(html: String): List<String>? =
        parseIngredientSections(Ksoup.parse(html))

    private fun parseIngredientSections(document: Element): List<String>? {
        val groups = document.select("div.wprm-recipe-ingredient-group")
        if (groups.isEmpty()) return null

        val lines = mutableListOf<String>()
        var sawHeader = false
        for (group in groups) {
            val name =
                group.selectFirst(".wprm-recipe-ingredient-group-name")?.text()?.trim().orEmpty()
            if (name.isNotEmpty()) {
                lines += IngredientSection.header(name)
                sawHeader = true
            }
            for (item in group.select("li.wprm-recipe-ingredient")) {
                val text = ingredientText(item)
                if (text.isNotEmpty()) lines += text
            }
        }

        // Only worth overriding the JSON-LD list when we actually found section headers.
        return if (sawHeader && lines.isNotEmpty()) lines else null
    }

    /** Reconstructs a single WPRM ingredient line from its amount/unit/name/notes spans. */
    private fun ingredientText(item: Element): String {
        val name = item.selectFirst(".wprm-recipe-ingredient-name")?.text()?.trim().orEmpty()
        if (name.isEmpty()) {
            // Non-structured markup: fall back to the checkbox label, else the raw list text.
            return item.selectFirst("input.wprm-checkbox")?.attr("aria-label")?.trim()
                ?: item.text().trim()
        }
        val amount = item.selectFirst(".wprm-recipe-ingredient-amount")?.text()?.trim().orEmpty()
        val unit = item.selectFirst(".wprm-recipe-ingredient-unit")?.text()?.trim().orEmpty()
        val notes = item.selectFirst(".wprm-recipe-ingredient-notes")?.text()?.trim().orEmpty()
        val head = listOf(amount, unit, name).filter { it.isNotEmpty() }.joinToString(" ")
        return if (notes.isNotEmpty()) "$head, $notes" else head
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
        // A real mobile-browser User-Agent. Some sites' WAFs return 403 to bot-style or
        // spoofed desktop-Chrome User-Agents, but accept genuine mobile-browser strings.
        private const val USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 " +
                "(KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"

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
