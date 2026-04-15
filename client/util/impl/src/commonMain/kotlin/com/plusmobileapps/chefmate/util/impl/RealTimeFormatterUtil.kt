package com.plusmobileapps.chefmate.util.impl

import chefmate.client.util.public.generated.resources.Res
import chefmate.client.util.public.generated.resources.time_format_hours
import chefmate.client.util.public.generated.resources.time_format_hours_minutes
import chefmate.client.util.public.generated.resources.time_format_minutes
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.util.TimeFormatterUtil
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class RealTimeFormatterUtil : TimeFormatterUtil {
    override fun formatMinutes(totalMinutes: Int): TextData {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 ->
                PhraseModel(
                    Res.string.time_format_hours_minutes,
                    "hours" to FixedString(hours.toString()),
                    "minutes" to FixedString(minutes.toString()),
                )
            hours > 0 ->
                PhraseModel(
                    Res.string.time_format_hours,
                    "hours" to FixedString(hours.toString()),
                )
            else ->
                PhraseModel(
                    Res.string.time_format_minutes,
                    "minutes" to FixedString(minutes.toString()),
                )
        }
    }
}
