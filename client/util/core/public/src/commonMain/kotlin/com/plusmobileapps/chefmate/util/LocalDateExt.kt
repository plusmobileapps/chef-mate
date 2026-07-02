package com.plusmobileapps.chefmate.util

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

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
