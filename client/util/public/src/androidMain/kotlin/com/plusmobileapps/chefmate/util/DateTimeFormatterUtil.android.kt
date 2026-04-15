@file:RequiresApi(Build.VERSION_CODES.O)
@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime

actual class DateTimeFormatterUtilImpl(locale: Locale = Locale.getDefault()) :
    DateTimeFormatterUtil {
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", locale)
    private val shortDateFormatter = DateTimeFormatter.ofPattern("MMM dd", locale)
    private val longDateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", locale)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss a", locale)

    actual override fun shortDate(instant: Instant, timeZone: TimeZone): String =
        instant.toJavaDateTime(timeZone).format(shortDateFormatter)

    actual override fun longDate(instant: Instant, timeZone: TimeZone): String =
        instant.toJavaDateTime(timeZone).format(longDateFormatter)

    actual override fun formatTime(instant: Instant, timeZone: TimeZone): String =
        instant.toJavaDateTime(timeZone).format(timeFormatter)

    actual override fun formatDateTime(instant: Instant, timeZone: TimeZone): String =
        instant.toJavaDateTime(timeZone).format(dateTimeFormatter)

    private fun Instant.toJavaDateTime(timeZone: TimeZone): java.time.LocalDateTime =
        toLocalDateTime(timeZone).toJavaLocalDateTime()
}
