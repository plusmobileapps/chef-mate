package com.plusmobileapps.chefmate.util.testing

import com.plusmobileapps.chefmate.util.DateTimeUtil
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlinx.datetime.Instant as KotlinxInstant

class FakeDateTimeUtil(
    var fakeNow: Instant = Instant.parse("2024-01-01T00:00:00Z"),
    var fakeTimeZone: TimeZone = TimeZone.UTC,
) : DateTimeUtil {
    override val now: Instant
        get() = fakeNow

    override val currentTimezone: TimeZone
        get() = fakeTimeZone

    override fun shortDate(
        instant: Instant,
        timeZone: TimeZone,
    ): String {
        val localDateTime = KotlinxInstant.fromEpochMilliseconds(instant.toEpochMilliseconds()).toLocalDateTime(timeZone)
        return "${localDateTime.month.ordinal + 1}/${localDateTime.dayOfMonth}/${localDateTime.year}"
    }

    override fun longDate(
        instant: Instant,
        timeZone: TimeZone,
    ): String {
        val localDateTime = KotlinxInstant.fromEpochMilliseconds(instant.toEpochMilliseconds()).toLocalDateTime(timeZone)
        return "${localDateTime.month.ordinal + 1}/${localDateTime.dayOfMonth}/${localDateTime.year}"
    }

    override fun formatTime(
        instant: Instant,
        timeZone: TimeZone,
    ): String {
        val localDateTime = KotlinxInstant.fromEpochMilliseconds(instant.toEpochMilliseconds()).toLocalDateTime(timeZone)
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    override fun formatDateTime(
        instant: Instant,
        timeZone: TimeZone,
    ): String = "${shortDate(instant, timeZone)} ${formatTime(instant, timeZone)}"
}
