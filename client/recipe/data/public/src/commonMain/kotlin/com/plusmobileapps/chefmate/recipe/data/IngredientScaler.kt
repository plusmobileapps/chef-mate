package com.plusmobileapps.chefmate.recipe.data

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

/**
 * Scales the quantity at the start of a recipe ingredient line up or down by a factor.
 *
 * Only the *leading* amount is touched — the first number (or range of numbers) at the very start
 * of the line, before the unit and ingredient name. A line with no leading amount (e.g. `Salt to
 * taste`) and any [IngredientSection] header are returned unchanged, as is any line when [factor]
 * is `1.0`.
 *
 * The parser understands the amount formats recipes actually use:
 * - whole numbers and decimals — `2`, `1.5`
 * - ASCII fractions and mixed numbers — `1/2`, `1 1/2`
 * - Unicode vulgar fractions, optionally with a whole part — `½`, `1½`, `1 ½`
 * - ranges joined by a hyphen or `to` — `2-3`, `2 to 3`
 *
 * Results are re-rendered with a Unicode vulgar fraction when the scaled value lands close to a
 * common one (halves, thirds, quarters, …); otherwise a trimmed decimal is used.
 */
object IngredientScaler {

    /** Vulgar-fraction glyph -> its value, for parsing an amount that uses one. */
    private val VULGAR_VALUES: Map<Char, Double> =
        mapOf(
            '¼' to 1.0 / 4,
            '½' to 1.0 / 2,
            '¾' to 3.0 / 4,
            '⅓' to 1.0 / 3,
            '⅔' to 2.0 / 3,
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
            '⅐' to 1.0 / 7,
            '⅑' to 1.0 / 9,
            '⅒' to 1.0 / 10,
            '↉' to 0.0,
        )

    /**
     * Common fractional parts we render back with a glyph, most-preferred rounding last so ties
     * favour the tidier fraction. Value -> glyph.
     */
    private val RENDER_FRACTIONS: List<Pair<Double, Char>> =
        listOf(
            1.0 / 8 to '⅛',
            1.0 / 6 to '⅙',
            1.0 / 5 to '⅕',
            1.0 / 4 to '¼',
            1.0 / 3 to '⅓',
            3.0 / 8 to '⅜',
            2.0 / 5 to '⅖',
            1.0 / 2 to '½',
            3.0 / 5 to '⅗',
            5.0 / 8 to '⅝',
            2.0 / 3 to '⅔',
            3.0 / 4 to '¾',
            4.0 / 5 to '⅘',
            5.0 / 6 to '⅚',
            7.0 / 8 to '⅞',
        )

    private const val TOLERANCE = 0.02

    private val vulgarChars = VULGAR_VALUES.keys.joinToString("")

    // A single amount, tried most-specific first: mixed ASCII ("1 1/2"), ASCII fraction ("1/2"),
    // a whole number with a trailing vulgar glyph ("1½" / "1 ½"), a plain decimal/integer, or a
    // bare vulgar glyph ("½"). Kept as a string so it can be reused inside the leading-amount
    // regex.
    private val amount =
        """(?:\d+\s+\d+\s*/\s*\d+|\d+\s*/\s*\d+|\d+\s*[$vulgarChars]|\d+(?:\.\d+)?|[$vulgarChars])"""

    // Leading amount at the very start of the line, with an optional range tail. Groups:
    // 1 = leading whitespace, 2 = first amount, 3 = range separator, 4 = second amount.
    private val LEADING_AMOUNT = Regex("""^(\s*)($amount)(?:(\s*(?:-|–|—|to)\s*)($amount))?""")

    /** Scales the leading amount of [line] by [factor], leaving the rest of the line untouched. */
    fun scale(line: String, factor: Double): String {
        if (factor == 1.0) return line
        if (IngredientSection.isHeader(line)) return line

        val match = LEADING_AMOUNT.find(line) ?: return line
        val (leadingWs, first, separator, second) = match.destructured

        val scaledFirst = format(parseAmount(first) * factor)
        val replacement =
            if (second.isNotEmpty()) {
                val scaledSecond = format(parseAmount(second) * factor)
                "$leadingWs$scaledFirst$separator$scaledSecond"
            } else {
                "$leadingWs$scaledFirst"
            }
        return replacement + line.substring(match.range.last + 1)
    }

    /** Parses a single matched amount token (fraction / mixed / decimal / vulgar) to a Double. */
    private fun parseAmount(token: String): Double {
        val normalized = token.trim().replace(Regex("""\s*/\s*"""), "/")

        val last = normalized.last()
        if (last in VULGAR_VALUES) {
            val wholePart = normalized.dropLast(1).trim()
            val whole = if (wholePart.isEmpty()) 0.0 else wholePart.toDouble()
            return whole + VULGAR_VALUES.getValue(last)
        }

        if ('/' in normalized) {
            val parts = normalized.split(Regex("""\s+""")).filter { it.isNotEmpty() }
            return if (parts.size == 2) {
                parts[0].toDouble() + parseFraction(parts[1])
            } else {
                parseFraction(normalized)
            }
        }

        return normalized.toDouble()
    }

    private fun parseFraction(fraction: String): Double {
        val (numerator, denominator) = fraction.split("/")
        val denom = denominator.toDouble()
        return if (denom == 0.0) 0.0 else numerator.toDouble() / denom
    }

    /**
     * Renders a scaled value as a whole number, a whole-plus-vulgar-fraction, or a trimmed decimal.
     */
    private fun format(value: Double): String {
        val whole = floor(value).toInt()
        val frac = value - whole

        if (frac < TOLERANCE) return whole.toString()
        if (frac > 1 - TOLERANCE) return (whole + 1).toString()

        val glyph = RENDER_FRACTIONS.minByOrNull { abs(it.first - frac) }
        if (glyph != null && abs(glyph.first - frac) <= TOLERANCE) {
            return if (whole == 0) glyph.second.toString() else "$whole${glyph.second}"
        }

        return trimDecimal(value)
    }

    private fun trimDecimal(value: Double): String {
        val rounded = round(value * 100) / 100
        val asLong = rounded.toLong()
        return if (rounded == asLong.toDouble()) asLong.toString() else rounded.toString()
    }
}
