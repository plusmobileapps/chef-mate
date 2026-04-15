package com.plusmobileapps.chefmate.util

import com.plusmobileapps.chefmate.text.TextData

/**
 * Formats a duration in minutes into a localized [TextData] string.
 *
 * @sample 45 → "45 min"
 * @sample 60 → "1 hr"
 * @sample 90 → "1 hr 30 min"
 */
interface TimeFormatterUtil {
    fun formatMinutes(totalMinutes: Int): TextData
}
