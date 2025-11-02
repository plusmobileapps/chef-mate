package com.plusmobileapps.chefmate.util.testing

import com.plusmobileapps.chefmate.util.DateTimeUtil
import kotlin.time.Instant

class FakeDateTimeUtil(
    private val fakeNow: Instant = Instant.parse("2024-01-01T00:00:00Z"),
) : DateTimeUtil {
    override val now: Instant
        get() = fakeNow
}
