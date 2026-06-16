@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

// java.time is unavailable on wasmJs, so formatting is built with the kotlinx-datetime format DSL.
actual class DateTimeFormatterUtilImpl : DateTimeFormatterUtil {
    private val timeFormat = LocalDateTime.Format {
        amPmHour()
        char(':')
        minute()
        char(' ')
        amPmMarker("AM", "PM")
    }
    private val shortDateFormat = LocalDateTime.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        dayOfMonth()
    }
    private val longDateFormat = LocalDateTime.Format {
        monthName(MonthNames.ENGLISH_FULL)
        char(' ')
        dayOfMonth()
        chars(", ")
        year()
    }
    private val mediumDateFormat = LocalDateTime.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        dayOfMonth(Padding.NONE)
        chars(", ")
        year()
    }
    private val dateTimeFormat = LocalDateTime.Format {
        year()
        char('-')
        monthNumber()
        char('-')
        dayOfMonth()
        char(' ')
        hour()
        char(':')
        minute()
        char(':')
        second()
        char(' ')
        amPmMarker("AM", "PM")
    }

    override fun shortDate(instant: Instant, timeZone: TimeZone): String =
        instant.toLocalDateTime(timeZone).format(shortDateFormat)

    override fun longDate(instant: Instant, timeZone: TimeZone): String =
        instant.toLocalDateTime(timeZone).format(longDateFormat)

    override fun mediumDate(instant: Instant, timeZone: TimeZone): String =
        instant.toLocalDateTime(timeZone).format(mediumDateFormat)

    override fun formatTime(instant: Instant, timeZone: TimeZone): String =
        instant.toLocalDateTime(timeZone).format(timeFormat)

    override fun formatDateTime(instant: Instant, timeZone: TimeZone): String =
        instant.toLocalDateTime(timeZone).format(dateTimeFormat)
}
