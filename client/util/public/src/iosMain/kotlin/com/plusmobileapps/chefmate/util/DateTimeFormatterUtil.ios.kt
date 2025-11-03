@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import kotlinx.datetime.TimeZone
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeZoneWithName
import kotlin.time.Instant

actual class DateTimeFormatterUtilImpl : DateTimeFormatterUtil {
    private val dateFormatter =
        NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd HH:mm:ss"
        }

    private val shortDateFormatter =
        NSDateFormatter().apply {
            dateFormat = "MMM dd"
        }

    private val longDateFormatter =
        NSDateFormatter().apply {
            dateFormat = "MMMM dd, yyyy"
        }

    private val timeFormatter =
        NSDateFormatter().apply {
            dateFormat = "hh:mm a"
        }

    override fun shortDate(
        instant: Instant,
        timeZone: TimeZone,
    ): String {
        val nsDate = NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
        shortDateFormatter.timeZone = NSTimeZone.timeZoneWithName(timeZone.id)!!
        return shortDateFormatter.stringFromDate(nsDate)
    }

    override fun longDate(
        instant: Instant,
        timeZone: TimeZone,
    ): String {
        val nsDate = NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
        longDateFormatter.timeZone = NSTimeZone.timeZoneWithName(timeZone.id)!!
        return longDateFormatter.stringFromDate(nsDate)
    }

    override fun formatTime(
        instant: Instant,
        timeZone: TimeZone,
    ): String {
        val nsDate = NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
        timeFormatter.timeZone = NSTimeZone.timeZoneWithName(timeZone.id)!!
        return timeFormatter.stringFromDate(nsDate)
    }

    override fun formatDateTime(
        instant: Instant,
        timeZone: TimeZone,
    ): String {
        val nsDate = NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
        dateFormatter.timeZone = NSTimeZone.timeZoneWithName(timeZone.id)!!
        return dateFormatter.stringFromDate(nsDate)
    }
}
