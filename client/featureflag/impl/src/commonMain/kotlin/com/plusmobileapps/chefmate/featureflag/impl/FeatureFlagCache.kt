package com.plusmobileapps.chefmate.featureflag.impl

import com.plusmobileapps.chefmate.di.AppScope
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json

@Inject
@SingleIn(AppScope::class)
class FeatureFlagCache(private val settings: Settings) {

    private val json = Json { ignoreUnknownKeys = true }

    fun load(): List<FeatureFlagRow> {
        val raw = settings.getStringOrNull(KEY) ?: return emptyList()
        return runCatching { json.decodeFromString<List<FeatureFlagRow>>(raw) }
            .getOrElse { emptyList() }
    }

    fun save(rows: List<FeatureFlagRow>) {
        settings.putString(KEY, json.encodeToString(rows))
    }

    private companion object {
        const val KEY = "flags.cache.v1"
    }
}
