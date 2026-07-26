package com.plusmobileapps.chefmate.grocery.data

/**
 * Combines two raw ingredient lines (e.g. "2 cups flour" + "1 cup flour") that [IngredientParser]
 * resolves to the same item name, so adding a recipe's ingredients to a grocery list merges into
 * one row instead of creating a duplicate. Amounts are summed when both lines carry a parseable
 * quantity in the same unit; otherwise the two quantities are concatenated rather than guessed at.
 */
object IngredientQuantityMerger {

    fun merge(existingLine: String, newLine: String): String {
        val existing = IngredientParser.parse(existingLine)
        val incoming = IngredientParser.parse(newLine)
        val name = existing.name.ifBlank { incoming.name }
        val mergedQuantity = mergeQuantities(existing.quantity, incoming.quantity)
        return if (mergedQuantity == null) name else "$mergedQuantity $name"
    }

    private fun mergeQuantities(a: String?, b: String?): String? {
        val parsedA = a?.let { parseAmountAndUnit(it) }
        val parsedB = b?.let { parseAmountAndUnit(it) }

        // A bare item (no quantity at all) is an implicit count of 1, so re-adding "red bell
        // pepper" to a list that already has one yields "2 red bell pepper" instead of silently
        // doing nothing. This only combines when both sides are plain counts (unit == null); a
        // bare add against a unit-bearing quantity like "2 cups flour" can't be summed, so the
        // existing quantity is kept.
        val amountA = if (a == null) 1.0 else parsedA?.amount
        val amountB = if (b == null) 1.0 else parsedB?.amount
        val unitA = if (a == null) null else parsedA?.unit
        val unitB = if (b == null) null else parsedB?.unit

        return if (amountA != null && amountB != null && unitA == unitB) {
            formatQuantity(amountA + amountB, unitA)
        } else {
            // Units differ or a quantity couldn't be parsed. Keep the side that actually
            // specifies a quantity; if both do, list them so neither is lost.
            when {
                a == null -> b
                b == null -> a
                else -> "$a + $b"
            }
        }
    }

    private data class AmountAndUnit(val amount: Double, val unit: String?)

    private fun parseAmountAndUnit(quantity: String): AmountAndUnit? {
        val trimmed = quantity.trim()
        val amountMatch = LEADING_AMOUNT_REGEX.find(trimmed) ?: return null
        val amount = parseAmount(amountMatch.value) ?: return null
        val unitWord = trimmed.substring(amountMatch.value.length).trim().lowercase()
        val unit = unitWord.ifBlank { null }?.let { UNIT_SYNONYMS[it] ?: it }
        return AmountAndUnit(amount, unit)
    }

    private fun parseAmount(token: String): Double? {
        MIXED_NUMBER_REGEX.matchEntire(token)?.let { match ->
            val (whole, num, den) = match.destructured
            val denominator = den.toDoubleOrNull() ?: return null
            if (denominator == 0.0) return null
            return whole.toDouble() + num.toDouble() / denominator
        }
        FRACTION_REGEX.matchEntire(token)?.let { match ->
            val (num, den) = match.destructured
            val denominator = den.toDoubleOrNull() ?: return null
            if (denominator == 0.0) return null
            return num.toDouble() / denominator
        }
        VULGAR_FRACTIONS[token.lastOrNull()]?.let { fraction ->
            val wholePart = token.dropLast(1)
            val whole = if (wholePart.isEmpty()) 0.0 else wholePart.toDoubleOrNull() ?: return null
            return whole + fraction
        }
        return token.toDoubleOrNull()
    }

    private fun formatQuantity(amount: Double, unit: String?): String {
        val rounded = kotlin.math.round(amount * 100) / 100
        val amountText =
            if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString()
            else rounded.toString()
        if (unit == null) return amountText
        val display = UNIT_DISPLAY[unit]
        val unitText =
            when {
                display == null -> unit
                rounded == 1.0 -> display.first
                else -> display.second
            }
        return "$amountText $unitText"
    }

    // Matches a leading amount token: a mixed number ("1 1/2"), a mixed unicode fraction ("1½"),
    // a bare unicode fraction ("½"), a simple fraction ("1/2"), a decimal, or a plain integer.
    // Order matters — longer/more specific alternatives must come first.
    private val LEADING_AMOUNT_REGEX =
        Regex("""^\d+\s+\d+/\d+|^\d+[½⅓⅔¼¾⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞]|^[½⅓⅔¼¾⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞]|^\d+/\d+|^\d+\.\d+|^\d+""")
    private val MIXED_NUMBER_REGEX = Regex("""^(\d+)\s+(\d+)/(\d+)$""")
    private val FRACTION_REGEX = Regex("""^(\d+)/(\d+)$""")

    private val VULGAR_FRACTIONS: Map<Char, Double> =
        mapOf(
            '½' to 1.0 / 2,
            '⅓' to 1.0 / 3,
            '⅔' to 2.0 / 3,
            '¼' to 1.0 / 4,
            '¾' to 3.0 / 4,
            '⅕' to 1.0 / 5,
            '⅖' to 2.0 / 5,
            '⅗' to 3.0 / 5,
            '⅘' to 4.0 / 5,
            '⅙' to 1.0 / 6,
            '⅚' to 5.0 / 6,
            '⅛' to 1.0 / 8,
            '⅜' to 3.0 / 8,
            '⅝' to 5.0 / 8,
            '⅞' to 7.0 / 8,
        )

    private val UNIT_SYNONYMS: Map<String, String> = buildMap {
        fun canonical(unit: String, vararg aliases: String) {
            put(unit, unit)
            aliases.forEach { put(it, unit) }
        }
        canonical("cup", "cups")
        canonical("tbsp", "tablespoon", "tablespoons")
        canonical("tsp", "teaspoon", "teaspoons")
        canonical("oz", "ounce", "ounces")
        canonical("lb", "lbs", "pound", "pounds")
        canonical("g", "gram", "grams")
        canonical("kg", "kilogram", "kilograms")
        canonical("ml", "milliliter", "milliliters")
        canonical("l", "liter", "liters")
        canonical("qt", "quart", "quarts")
        canonical("gal", "gallon", "gallons")
        canonical("pt", "pint", "pints")
        canonical("can", "cans")
        canonical("bottle", "bottles")
        canonical("package", "packages", "pkg", "pkgs")
        canonical("bag", "bags")
        canonical("bunch", "bunches")
        canonical("head", "heads")
        canonical("clove", "cloves")
        canonical("stalk", "stalks")
        canonical("slice", "slices")
        canonical("piece", "pieces")
        canonical("stick", "sticks")
        canonical("sprig", "sprigs")
        canonical("ear", "ears")
    }

    // Only units whose plural form isn't identical to the singular need an entry; anything
    // missing (abbreviations like "tbsp", "oz", metric units) is used as-is regardless of count.
    private val UNIT_DISPLAY: Map<String, Pair<String, String>> =
        mapOf(
            "cup" to ("cup" to "cups"),
            "lb" to ("lb" to "lbs"),
            "can" to ("can" to "cans"),
            "bottle" to ("bottle" to "bottles"),
            "package" to ("package" to "packages"),
            "bag" to ("bag" to "bags"),
            "bunch" to ("bunch" to "bunches"),
            "head" to ("head" to "heads"),
            "clove" to ("clove" to "cloves"),
            "stalk" to ("stalk" to "stalks"),
            "slice" to ("slice" to "slices"),
            "piece" to ("piece" to "pieces"),
            "stick" to ("stick" to "sticks"),
            "sprig" to ("sprig" to "sprigs"),
            "ear" to ("ear" to "ears"),
        )
}
