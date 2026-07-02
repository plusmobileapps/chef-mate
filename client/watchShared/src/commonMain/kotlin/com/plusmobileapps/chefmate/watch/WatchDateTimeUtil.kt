package com.plusmobileapps.chefmate.watch

import com.plusmobileapps.chefmate.util.DateTimeUtil
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Minimal [DateTimeUtil] for the watch DI graph. The production binding (`RealDateTimeUtil`) lives
 * in the Compose-coupled `util:impl` module, which has no watchOS target. The grocery repository
 * only reads [now] for timestamps; the formatting methods are implemented with plain ISO output
 * since the watch UI formats dates natively in SwiftUI.
 */
class WatchDateTimeUtil : DateTimeUtil {
    override val now: Instant
        get() = Clock.System.now()

    override val currentTimezone: TimeZone
        get() = TimeZone.currentSystemDefault()

    override fun today(): LocalDate = now.toLocalDateTime(currentTimezone).date

    override fun shortDate(instant: Instant, timeZone: TimeZone): String =
        instant.toLocalDateTime(timeZone).date.toString()

    override fun longDate(instant: Instant, timeZone: TimeZone): String =
        instant.toLocalDateTime(timeZone).date.toString()

    override fun formatTime(instant: Instant, timeZone: TimeZone): String =
        instant.toLocalDateTime(timeZone).time.toString()

    override fun formatDateTime(instant: Instant, timeZone: TimeZone): String =
        instant.toLocalDateTime(timeZone).toString()

    override fun formatMediumDate(date: LocalDate, timeZone: TimeZone): String = date.toString()
}
