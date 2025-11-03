package com.plusmobileapps.chefmate.util

import kotlinx.datetime.TimeZone
import kotlin.time.Instant

interface DateTimeUtil {
    val now: Instant

    val currentTimezone: TimeZone
}
