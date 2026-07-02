package com.plusmobileapps.chefmate.util

import kotlin.time.Instant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

interface DateTimeUtil {
    val now: Instant

    val currentTimezone: TimeZone

    fun today(): LocalDate

    /**
     * Formats the given [Instant] into a short date string.
     *
     * @sample "Mar 23"
     */
    fun shortDate(instant: Instant, timeZone: TimeZone = currentTimezone): String

    /**
     * Formats the given [Instant] into a long date string.
     *
     * @sample "March 23, 2021"
     */
    fun longDate(instant: Instant, timeZone: TimeZone = currentTimezone): String

    /**
     * Formats the given [Instant] into a time string.
     *
     * @sample "12:00 PM"
     */
    fun formatTime(instant: Instant, timeZone: TimeZone = currentTimezone): String

    /**
     * Formats the given [Instant] into a date and time string.
     *
     * @sample "2021-09-01 12:00 PM"
     */
    fun formatDateTime(instant: Instant, timeZone: TimeZone = currentTimezone): String

    fun formatLocalDate(date: LocalDate, timeZone: TimeZone = currentTimezone): String {
        val startOfDay = date.toInstantAtStartOfDay(timeZone)
        return longDate(startOfDay, timeZone)
    }

    /**
     * Formats the given [LocalDate] into a medium date string.
     *
     * @sample "Jan 5, 2026"
     */
    fun formatMediumDate(date: LocalDate, timeZone: TimeZone = currentTimezone): String

    /**
     * Formats the given [LocalDate] into a short day-of-week and date string.
     *
     * @sample "Sun, Apr 12"
     */
    fun formatShortDayDate(date: LocalDate, timeZone: TimeZone = currentTimezone): String {
        val startOfDay = date.toInstantAtStartOfDay(timeZone)
        val dayPrefix = date.dayOfWeek.shortName()
        return "$dayPrefix, ${shortDate(startOfDay, timeZone)}"
    }
}

private fun DayOfWeek.shortName(): String =
    when (this) {
        DayOfWeek.SUNDAY -> "Sun"
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        else -> ""
    }
