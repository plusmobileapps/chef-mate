package com.plusmobileapps.chefmate.util.impl

import com.plusmobileapps.chefmate.util.DateTimeFormatterUtil
import com.plusmobileapps.chefmate.util.DateTimeUtil
import kotlinx.datetime.TimeZone
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import kotlin.time.Clock
import kotlin.time.Instant

@Inject
@ContributesBinding(AppScope::class, boundType = DateTimeUtil::class)
class RealDateTimeUtil(
    private val dateTimeFormatter: DateTimeFormatterUtil,
) : DateTimeUtil {
    override val now: Instant
        get() = Clock.System.now()

    override val currentTimezone: TimeZone
        get() = TimeZone.currentSystemDefault()

    override fun shortDate(
        instant: Instant,
        timeZone: TimeZone,
    ): String = dateTimeFormatter.shortDate(instant, timeZone)

    override fun longDate(
        instant: Instant,
        timeZone: TimeZone,
    ): String = dateTimeFormatter.longDate(instant, timeZone)

    override fun formatTime(
        instant: Instant,
        timeZone: TimeZone,
    ): String = dateTimeFormatter.formatTime(instant, timeZone)

    override fun formatDateTime(
        instant: Instant,
        timeZone: TimeZone,
    ): String = dateTimeFormatter.formatDateTime(instant, timeZone)
}
