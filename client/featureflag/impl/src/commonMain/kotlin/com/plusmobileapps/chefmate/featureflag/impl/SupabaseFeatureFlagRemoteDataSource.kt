package com.plusmobileapps.chefmate.featureflag.impl

import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

interface FeatureFlagRemoteDataSource {
    suspend fun fetch(): List<FeatureFlagRow>
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SupabaseFeatureFlagRemoteDataSource(private val client: SupabaseClient) :
    FeatureFlagRemoteDataSource {

    // Request only the columns FeatureFlagRow knows about (the table also carries admin-only
    // columns like `archived`/`description`/timestamps that would fail strict decoding) and skip
    // archived flags — those are retired and must never reach clients.
    override suspend fun fetch(): List<FeatureFlagRow> =
        client.from(TABLE).select(COLUMNS) { filter { eq("archived", false) } }.decodeList()

    private companion object {
        const val TABLE = "feature_flags"
        val COLUMNS =
            Columns.list(
                "key",
                "value_type",
                "value",
                "enabled",
                "rollout_percent",
                "platforms",
                "min_version",
                "max_version",
                "user_ids",
            )
    }
}
