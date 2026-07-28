package com.plusmobileapps.chefmate.browser.impl

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
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
                val data = parseRecipeJsonText(script.data(), url) ?: continue
                // schema.org flattens grouped ingredients, so recover any sub-section headers
                // (e.g. "For the sauce:") from the page markup when present.
                val grouped = parseIngredientSections(document)
                return@withContext if (grouped != null) data.copy(ingredients = grouped) else data
            }

            // No JSON-LD recipe — fall back to microdata/microformat markup.
            parseMicrodataRecipe(document, url)?.let {
                return@withContext it
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

    /**
     * Fallback for pages that describe their recipe with schema.org **microdata** or an
     * **hrecipe/h-recipe microformat** instead of JSON-LD — most notably the Jetpack Recipe
     * shortcode used by Smitten Kitchen and other WordPress.com-hosted blogs. Returns `null` when
     * the page has no such markup, so the caller can report that no recipe was found.
     */
    internal fun parseMicrodataRecipe(html: String, sourceUrl: String): ExtractedRecipeData? =
        parseMicrodataRecipe(Ksoup.parse(html), sourceUrl)

    private fun parseMicrodataRecipe(document: Document, sourceUrl: String): ExtractedRecipeData? {
        val root = document.selectFirst(RECIPE_ROOT_SELECTOR) ?: return null

        val ingredients =
            root.select(INGREDIENT_SELECTOR).mapNotNull { it.text().trim().nullIfBlank() }
        val directions = root.selectFirst(INSTRUCTIONS_SELECTOR)?.let(::textBlocks).orEmpty()
        // Both empty means we matched some unrelated markup, not a real recipe.
        if (ingredients.isEmpty() && directions.isEmpty()) return null

        val title =
            root.selectFirst(NAME_SELECTOR)?.text()?.trim().nullIfBlank()
                ?: document.metaContent("og:title")
                ?: ""

        return ExtractedRecipeData(
            title = title,
            description =
                root.selectFirst(DESCRIPTION_SELECTOR)?.text()?.trim().nullIfBlank()
                    ?: document.metaContent("og:description"),
            ingredients = ingredients,
            directions = directions,
            imageUrl =
                root.selectFirst(IMAGE_SELECTOR)?.imageUrl() ?: document.metaContent("og:image"),
            sourceUrl = sourceUrl,
            servings = root.selectFirst(YIELD_SELECTOR)?.text().firstNumber(),
            prepTime = root.selectFirst(PREP_TIME_SELECTOR).durationMinutes(),
            cookTime = root.selectFirst(COOK_TIME_SELECTOR).durationMinutes(),
            totalTime = root.selectFirst(TOTAL_TIME_SELECTOR).durationMinutes(),
            calories = root.selectFirst(CALORIES_SELECTOR)?.text().firstNumber(),
        )
    }

    private fun Document.metaContent(property: String): String? =
        selectFirst("meta[property='$property'], meta[name='$property']")
            ?.attr("content")
            ?.trim()
            .nullIfBlank()

    /** Microdata/microformat markup carries the image url on any of these, depending on the tag. */
    private fun Element.imageUrl(): String? =
        listOf("src", "content", "href").firstNotNullOfOrNull { attr(it).trim().nullIfBlank() }

    /**
     * Durations are published as an ISO-8601 `datetime`/`title` attribute in well-formed markup,
     * but plugins routinely put prose there instead (`datetime="1 hour, plus chopping"`), so fall
     * back to reading the value as English.
     */
    private fun Element?.durationMinutes(): Int? {
        if (this == null) return null
        val candidates =
            listOf(attr("datetime").trim(), attr("title").trim(), text().trim()).filter {
                it.isNotEmpty()
            }
        return candidates.firstNotNullOfOrNull { parseDuration(it) }
            ?: candidates.firstNotNullOfOrNull { parseLooseDuration(it) }
    }

    /**
     * Splits an element's rendered text into one entry per block-level child, so a directions
     * container written as a run of `<p>`s (or `<li>`s, or `<br>`-separated text) becomes one
     * direction per line. Text sitting directly on the container counts as its own block, which
     * matters because WordPress's auto-paragraph mangling routinely leaves the first step
     * unwrapped.
     */
    private fun textBlocks(element: Element): List<String> {
        val blocks = mutableListOf<String>()
        val current = StringBuilder()

        fun flush() {
            val text = current.toString().collapseWhitespace()
            if (text.isNotEmpty()) blocks += text
            current.clear()
        }

        fun visit(node: Node) {
            when (node) {
                is TextNode -> current.append(node.text())
                is Element -> {
                    val isBlock = node.tagName() in BLOCK_TAGS
                    if (isBlock) flush()
                    node.childNodes().forEach(::visit)
                    if (isBlock) flush()
                }
            }
        }

        element.childNodes().forEach(::visit)
        flush()
        return blocks
    }

    private fun findRecipeJson(jsonText: String): JsonObject? =
        findRecipeInElement(json.parseToJsonElement(jsonText))

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

    /**
     * Returns `null` for anything that isn't a well-formed schema.org `Recipe`, including malformed
     * JSON and unexpected value shapes, so one bad `ld+json` block on a page can't stop us from
     * trying the remaining blocks — or the microdata fallback.
     */
    internal fun parseRecipeJsonText(jsonText: String, sourceUrl: String): ExtractedRecipeData? =
        runCatching {
            findRecipeJson(jsonText)?.let { parseRecipeFromJson(it, sourceUrl) }
        }
        .getOrNull()

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
            is JsonArray -> yield.firstOrNull()?.jsonPrimitive?.contentOrNull.firstNumber()
            else -> yield.jsonPrimitive.contentOrNull.firstNumber()
        }
    }

    private fun parseCalories(obj: JsonObject): Int? {
        val nutrition = obj["nutrition"]?.jsonObject ?: return null
        return nutrition["calories"]?.jsonPrimitive?.contentOrNull.firstNumber()
    }

    companion object {
        // A real mobile-browser User-Agent. Some sites' WAFs return 403 to bot-style or
        // spoofed desktop-Chrome User-Agents, but accept genuine mobile-browser strings.
        private const val USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 " +
                "(KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"

        // schema.org microdata first, then the older hrecipe/h-recipe microformat class names.
        private const val RECIPE_ROOT_SELECTOR =
            "[itemtype*=schema.org/Recipe], .hrecipe, .h-recipe"
        private const val NAME_SELECTOR = "[itemprop=name], .fn, .p-name"
        private const val DESCRIPTION_SELECTOR =
            "[itemprop=description], .summary, .p-summary, .e-summary"
        private const val INGREDIENT_SELECTOR =
            "[itemprop=recipeIngredient], [itemprop=ingredients], .ingredient, .p-ingredient"
        private const val INSTRUCTIONS_SELECTOR =
            "[itemprop=recipeInstructions], .instructions, .e-instructions"
        private const val IMAGE_SELECTOR = "[itemprop=image], .photo, .u-photo"
        private const val YIELD_SELECTOR = "[itemprop=recipeYield], .yield, .p-yield"
        private const val PREP_TIME_SELECTOR = "[itemprop=prepTime], .preptime, .dt-preptime"
        private const val COOK_TIME_SELECTOR = "[itemprop=cookTime], .cooktime, .dt-cooktime"
        private const val TOTAL_TIME_SELECTOR =
            "[itemprop=totalTime], .totaltime, .dt-totaltime, .duration, .dt-duration"
        private const val CALORIES_SELECTOR = "[itemprop=calories]"

        private val BLOCK_TAGS =
            setOf("p", "li", "div", "br", "tr", "h1", "h2", "h3", "h4", "h5", "h6")

        private val WHITESPACE = Regex("\\s+")
        private val NUMBER = Regex("\\d[\\d,]*")
        // "1 hour", "1 hr 20 min", "1.5 hours", "45 minutes"
        private val LOOSE_DURATION =
            Regex(
                "(\\d+(?:\\.\\d+)?)\\s*(hours?|hrs?|h|minutes?|mins?|m)\\b",
                RegexOption.IGNORE_CASE,
            )

        private fun String?.nullIfBlank(): String? = this?.takeIf { it.isNotBlank() }

        private fun String.collapseWhitespace(): String = replace(WHITESPACE, " ").trim()

        /**
         * Reads the leading count out of a free-form value. Yields are routinely written as a range
         * ("Servings: 6 to 8"), where stripping every non-digit would produce a nonsense 68.
         */
        private fun String?.firstNumber(): Int? =
            this?.let { NUMBER.find(it)?.value?.replace(",", "")?.toIntOrNull() }

        /** Parses an English duration ("1 hour, plus chopping") into minutes. */
        fun parseLooseDuration(text: String?): Int? {
            if (text == null) return null
            // Trailing "plus …" clauses describe unattended time we shouldn't add to the total.
            val head = text.split(Regex("\\bplus\\b", RegexOption.IGNORE_CASE)).first()
            var minutes = 0.0
            for (match in LOOSE_DURATION.findAll(head)) {
                val value = match.groupValues[1].toDoubleOrNull() ?: continue
                val isHours = match.groupValues[2].lowercase().startsWith("h")
                minutes += if (isHours) value * 60 else value
            }
            return minutes.toInt().takeIf { it > 0 }
        }

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
