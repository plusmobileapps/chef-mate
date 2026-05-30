package com.plusmobileapps.chefmate.recipe.importer.impl

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element

/**
 * Parses a Paprika/Apple-style recipe export (a zip of HTML files carrying schema.org microdata,
 * plus an `Images/` folder) into [ParsedArchiveRecipe]s. Every `.html` entry is scanned for
 * `itemtype` Recipe blocks; images referenced by each block are resolved against that file's folder
 * and pulled from the archive. Recipes are de-duplicated by normalized title (a table-of-contents
 * `index.html` can re-list the same recipes), preferring the copy whose image resolved.
 */
internal object RecipeArchiveParser {

    private val leadingStepNumber = Regex("^\\s*\\d+\\s*[.)]?\\s*")
    private val firstNumber = Regex("\\d+")
    private val caloriesPattern = Regex("(?i)calories:\\s*(\\d+)")
    private val hoursPattern = Regex("(\\d+)\\s*h")
    private val minutesPattern = Regex("(\\d+)\\s*m")

    fun parse(archive: ByteArray): List<ParsedArchiveRecipe> {
        val entries = ZipReader.read(archive).filterNot { it.name.contains("__MACOSX") }
        val imagesByPath =
            entries.filter { it.name.isImagePath() }.associate { it.name to it.bytes }

        val byTitle = LinkedHashMap<String, ParsedArchiveRecipe>()
        for (entry in entries) {
            if (!entry.name.endsWith(".html", ignoreCase = true)) continue
            val html = entry.bytes.decodeToString()
            val document = Ksoup.parse(html)
            val baseDir = entry.name.substringBeforeLast('/', "")
            for (recipeElement in document.select("[itemtype*=Recipe]")) {
                val recipe = parseRecipe(recipeElement, baseDir, imagesByPath) ?: continue
                val key = recipe.title.trim().lowercase()
                val existing = byTitle[key]
                if (existing == null || (!existing.hasImage && recipe.hasImage)) {
                    byTitle[key] = recipe
                }
            }
        }
        return byTitle.values.toList()
    }

    private fun parseRecipe(
        element: Element,
        baseDir: String,
        imagesByPath: Map<String, ByteArray>,
    ): ParsedArchiveRecipe? {
        val title = element.selectFirst("[itemprop=name]")?.text()?.trim().orEmpty()
        if (title.isBlank()) return null

        val ingredients =
            element
                .select("[itemprop=recipeIngredient]")
                .map { it.text().trim() }
                .filter { it.isNotBlank() }

        val directions =
            element
                .selectFirst("[itemprop=recipeInstructions]")
                ?.select("p")
                ?.map { it.text().trim().replace(leadingStepNumber, "") }
                ?.filter { it.isNotBlank() }
                .orEmpty()

        val description =
            element.selectFirst("[itemprop=description]")?.text()?.trim()?.ifBlank { null }
        val sourceUrl =
            element.selectFirst("[itemprop=url]")?.attr("href")?.trim()?.ifBlank { null }
        val servings =
            element.selectFirst("[itemprop=recipeYield]")?.text()?.let {
                firstNumber.find(it)?.value?.toIntOrNull()
            }
        val prepTime = element.selectFirst("[itemprop=prepTime]")?.text()?.let(::parseMinutes)
        val cookTime = element.selectFirst("[itemprop=cookTime]")?.text()?.let(::parseMinutes)
        val totalTime = element.selectFirst("[itemprop=totalTime]")?.text()?.let(::parseMinutes)
        val calories =
            element.selectFirst("[itemprop=nutrition]")?.text()?.let {
                caloriesPattern.find(it)?.groupValues?.get(1)?.toIntOrNull()
            }
        val starRating =
            element
                .selectFirst("[itemprop=aggregateRating]")
                ?.attr("value")
                ?.toIntOrNull()
                ?.coerceIn(0, 5)

        val imageSrc = element.selectFirst("img[itemprop=image]")?.attr("src")?.trim().orEmpty()
        val imagePath = if (imageSrc.isBlank()) null else resolvePath(baseDir, imageSrc)
        val imageBytes = imagePath?.let { imagesByPath[it] }

        return ParsedArchiveRecipe(
            title = title,
            description = description,
            ingredients = ingredients,
            directions = directions,
            sourceUrl = sourceUrl,
            servings = servings,
            prepTime = prepTime,
            cookTime = cookTime,
            totalTime = totalTime,
            calories = calories,
            starRating = starRating,
            imageBytes = imageBytes,
            imageExtension = imagePath?.substringAfterLast('.', "")?.ifBlank { null },
        )
    }

    /** Parses human-readable durations like "10 minutes" or "1 hour 30 minutes" into minutes. */
    private fun parseMinutes(text: String): Int? {
        val lower = text.lowercase()
        val hours = hoursPattern.find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val minutes = minutesPattern.find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val total = hours * 60 + minutes
        return if (total > 0) total else null
    }

    private fun String.isImagePath(): Boolean {
        val lower = lowercase()
        return lower.endsWith(".jpg") ||
            lower.endsWith(".jpeg") ||
            lower.endsWith(".png") ||
            lower.endsWith(".webp") ||
            lower.endsWith(".gif")
    }

    /**
     * Resolves a relative archive reference against [baseDir], collapsing `.` and `..` segments.
     */
    private fun resolvePath(baseDir: String, relative: String): String {
        val segments = ArrayDeque<String>()
        if (baseDir.isNotEmpty()) segments.addAll(baseDir.split('/'))
        for (segment in relative.split('/')) {
            when (segment) {
                "",
                "." -> {}
                ".." -> if (segments.isNotEmpty()) segments.removeLast()
                else -> segments.addLast(segment)
            }
        }
        return segments.joinToString("/")
    }
}
