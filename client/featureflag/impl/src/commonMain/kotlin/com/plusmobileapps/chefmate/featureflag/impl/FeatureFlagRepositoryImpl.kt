package com.plusmobileapps.chefmate.featureflag.impl

import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import com.plusmobileapps.chefmate.combineStates
import com.plusmobileapps.chefmate.currentPlatform
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.featureflag.FeatureFlag
import com.plusmobileapps.chefmate.featureflag.FeatureFlagOverrides
import com.plusmobileapps.chefmate.featureflag.FeatureFlags
import com.plusmobileapps.chefmate.featureflag.Override
import com.plusmobileapps.chefmate.mapState
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class FeatureFlagRepositoryImpl(
    private val evaluator: FeatureFlagEvaluator,
    private val cache: FeatureFlagCache,
    private val remote: FeatureFlagRemoteDataSource,
    private val overrides: FeatureFlagOverrides,
    private val identityProvider: BucketingIdentityProvider,
) : FeatureFlags {

    private val log = Logger.withTag("FeatureFlags")

    private val rows: MutableStateFlow<Map<String, FeatureFlagRow>> =
        MutableStateFlow(cache.load().associateBy { it.key })

    override fun <T : Any> valueOf(flag: FeatureFlag<T>): StateFlow<T> {
        val rowFlow = rows.mapState { it[flag.key] }
        val rowAndIdentity =
            combineStates(rowFlow, identityProvider.identity) { row, id -> row to id }
        return combineStates(rowAndIdentity, overrides.overrideOf(flag)) { (row, id), override ->
            resolve(flag, row, override, id)
        }
    }

    override suspend fun refresh() {
        try {
            val fetched = remote.fetch()
            rows.value = fetched.associateBy { it.key }
            cache.save(fetched)
        } catch (e: Throwable) {
            log.w(e) { "Failed to refresh feature flags; falling back to cached values." }
        }
    }

    private fun <T : Any> resolve(
        flag: FeatureFlag<T>,
        row: FeatureFlagRow?,
        override: Override<T>,
        identity: String,
    ): T =
        when (override) {
            is Override.ForceValue -> override.value
            Override.Default ->
                evaluator.evaluate(
                    flag = flag,
                    row = row,
                    context =
                        EvaluationContext(
                            identity = identity,
                            platform = currentPlatform,
                            versionName = BuildConfig.VERSION_NAME,
                        ),
                )
        }
}
