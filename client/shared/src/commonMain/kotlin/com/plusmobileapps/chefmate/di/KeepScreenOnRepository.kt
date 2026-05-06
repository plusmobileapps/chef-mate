package com.plusmobileapps.chefmate.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
@SingleIn(AppScope::class)
class KeepScreenOnRepository(settings: Settings) {
    private var pref by settings.boolean(KEY, defaultValue = true)

    private val _keepScreenOn = MutableStateFlow(pref)
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    fun toggle() {
        val newValue = !_keepScreenOn.value
        pref = newValue
        _keepScreenOn.value = newValue
    }

    companion object {
        private const val KEY = "recipe_detail_keep_screen_on"
    }
}
