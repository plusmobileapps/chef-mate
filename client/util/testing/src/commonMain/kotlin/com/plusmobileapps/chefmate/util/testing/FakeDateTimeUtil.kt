package com.plusmobileapps.chefmate.util.testing

import com.plusmobileapps.chefmate.util.DateTimeUtil
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

class FakeDateTimeUtil(
    var fakeNow: Instant = Instant.parse("2024-01-01T00:00:00Z"),
    var fakeTimeZone: TimeZone = TimeZone.UTC,
) : DateTimeUtil {
    override val now: Instant
        get() = fakeNow

    override val currentTimezone: TimeZone
        get() = fakeTimeZone
}
