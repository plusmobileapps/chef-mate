package com.plusmobileapps.chefmate.recipe.data

/**
 * Convention for sub-section headers inside a recipe's newline-delimited ingredient list.
 *
 * Some recipes group their ingredients under sub-headings — e.g. a main component and a separate
 * sauce:
 * ```
 * For the Peruvian chicken:
 * 1½ lbs chicken thighs
 * ½ cup soy sauce
 * For the green sauce:
 * ½ cup sour cream
 * 1 jalapeño
 * ```
 *
 * Headers are stored inline as their own line ending in a colon. They render bold on the recipe
 * detail / cook screens and are skipped when adding the recipe's ingredients to a grocery list.
 */
object IngredientSection {

    /** True when [line] is a section header rather than an actual ingredient. */
    fun isHeader(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.length > 1 && trimmed.endsWith(":")
    }

    /**
     * Formats [name] as a header line, normalizing it to end with a single colon. Returns an empty
     * string for a blank [name] so callers can drop it.
     */
    fun header(name: String): String {
        val trimmed = name.trim().trimEnd(':').trim()
        return if (trimmed.isEmpty()) "" else "$trimmed:"
    }
}
