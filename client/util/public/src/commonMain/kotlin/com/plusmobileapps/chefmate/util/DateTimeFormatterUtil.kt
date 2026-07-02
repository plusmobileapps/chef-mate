package com.plusmobileapps.chefmate.util

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

interface DateTimeFormatterUtil {
    /**
     * Formats the given [Instant] into a short date string.
     *
     * @sample "Mar 23"
     */
    fun shortDate(instant: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): String

    /**
     * Formats the given [Instant] into a long date string.
     *
     * @sample "March 23, 2021"
     */
    fun longDate(instant: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): String

    /**
     * Formats the given [Instant] into a time string.
     *
     * @sample "12:00 PM"
     */
    fun formatTime(instant: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): String

    /**
     * Formats the given [Instant] into a date and time string.
     *
     * @sample "2021-09-01 12:00 PM"
     */
    fun formatDateTime(
        instant: Instant,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): String

    /**
     * Formats the given [Instant] into a medium date string.
     *
     * @sample "Jan 5, 2026"
     */
    fun mediumDate(instant: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): String

    fun formatLocalDate(
        date: LocalDate,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): String {
        val startOfDay = date.toInstantAtStartOfDay(timeZone)
        return longDate(startOfDay, timeZone)
    }
}

expect class DateTimeFormatterUtilImpl : DateTimeFormatterUtil
