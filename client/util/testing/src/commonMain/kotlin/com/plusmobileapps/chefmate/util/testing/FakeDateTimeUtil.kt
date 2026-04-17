package com.plusmobileapps.chefmate.util.testing

import com.plusmobileapps.chefmate.util.DateTimeUtil
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

class FakeDateTimeUtil(
    var fakeNow: Instant = Instant.parse("2024-01-01T00:00:00Z"),
    var fakeTimeZone: TimeZone = TimeZone.UTC,
    var fakeToday: LocalDate? = null,
) : DateTimeUtil {
    override val now: Instant
        get() = fakeNow

    override val currentTimezone: TimeZone
        get() = fakeTimeZone

    override fun today(): LocalDate = fakeToday ?: kotlin.time.Clock.System.todayIn(fakeTimeZone)

    override fun shortDate(instant: Instant, timeZone: TimeZone): String {
        val localDateTime =
            Instant.fromEpochMilliseconds(instant.toEpochMilliseconds()).toLocalDateTime(timeZone)
        return "${localDateTime.month.ordinal + 1}/${localDateTime.dayOfMonth}/${localDateTime.year}"
    }

    override fun longDate(instant: Instant, timeZone: TimeZone): String {
        val localDateTime =
            Instant.fromEpochMilliseconds(instant.toEpochMilliseconds()).toLocalDateTime(timeZone)
        return "${localDateTime.month.ordinal + 1}/${localDateTime.dayOfMonth}/${localDateTime.year}"
    }

    override fun formatTime(instant: Instant, timeZone: TimeZone): String {
        val localDateTime =
            Instant.fromEpochMilliseconds(instant.toEpochMilliseconds()).toLocalDateTime(timeZone)
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    override fun formatDateTime(instant: Instant, timeZone: TimeZone): String =
        "${shortDate(instant, timeZone)} ${formatTime(instant, timeZone)}"
}
