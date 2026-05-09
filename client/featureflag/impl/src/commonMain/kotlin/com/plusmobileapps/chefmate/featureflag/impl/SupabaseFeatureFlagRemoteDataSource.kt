package com.plusmobileapps.chefmate.featureflag.impl

import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

interface FeatureFlagRemoteDataSource {
    suspend fun fetch(): List<FeatureFlagRow>
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SupabaseFeatureFlagRemoteDataSource(private val client: SupabaseClient) :
    FeatureFlagRemoteDataSource {

    override suspend fun fetch(): List<FeatureFlagRow> = client.from(TABLE).select().decodeList()

    private companion object {
        const val TABLE = "feature_flags"
    }
}
