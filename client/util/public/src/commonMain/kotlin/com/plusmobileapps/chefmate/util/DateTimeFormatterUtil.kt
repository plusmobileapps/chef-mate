package com.plusmobileapps.chefmate.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Instant

interface DateTimeFormatterUtil {
    /**
     * Formats the given [Instant] into a short date string.
     *
     * @sample "Mar 23"
     */
    fun shortDate(
        instant: Instant,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): String

    /**
     * Formats the given [Instant] into a long date string.
     *
     * @sample "March 23, 2021"
     */
    fun longDate(
        instant: Instant,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): String

    /**
     * Formats the given [Instant] into a time string.
     *
     * @sample "12:00 PM"
     */
    fun formatTime(
        instant: Instant,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): String

    /**
     * Formats the given [Instant] into a date and time string.
     *
     * @sample "2021-09-01 12:00 PM"
     */
    fun formatDateTime(
        instant: Instant,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): String

    fun formatLocalDate(
        date: LocalDate,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): String {
        val startOfDay = date.toInstantAtStartOfDay(timeZone)
        return longDate(startOfDay, timeZone)
    }
}

expect class DateTimeFormatterUtilImpl : DateTimeFormatterUtil

fun LocalDate.atStartOfDayIn(): LocalDateTime {
    // Assuming start of day is at 00:00, adjust if necessary for your use case
    return LocalDateTime(this.year, this.monthNumber, this.dayOfMonth, 0, 0)
}

fun LocalDate.toInstantAtStartOfDay(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    // Convert LocalDate to LocalDateTime at the start of the day (midnight)
    val startOfDay = this.atStartOfDayIn()
    // Convert LocalDateTime to Instant
    return startOfDay.toInstant(timeZone)
}
