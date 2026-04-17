package com.plusmobileapps.chefmate.util.impl

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.util.DateTimeFormatterUtil
import com.plusmobileapps.chefmate.util.DateTimeUtil
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Inject
@ContributesBinding(AppScope::class)
class RealDateTimeUtil(private val dateTimeFormatter: DateTimeFormatterUtil) : DateTimeUtil {
    override val now: Instant
        get() = Clock.System.now()

    override val currentTimezone: TimeZone
        get() = TimeZone.currentSystemDefault()

    override fun today(): LocalDate = Clock.System.todayIn(currentTimezone)

    override fun shortDate(instant: Instant, timeZone: TimeZone): String =
        dateTimeFormatter.shortDate(instant, timeZone)

    override fun longDate(instant: Instant, timeZone: TimeZone): String =
        dateTimeFormatter.longDate(instant, timeZone)

    override fun formatTime(instant: Instant, timeZone: TimeZone): String =
        dateTimeFormatter.formatTime(instant, timeZone)

    override fun formatDateTime(instant: Instant, timeZone: TimeZone): String =
        dateTimeFormatter.formatDateTime(instant, timeZone)
}
