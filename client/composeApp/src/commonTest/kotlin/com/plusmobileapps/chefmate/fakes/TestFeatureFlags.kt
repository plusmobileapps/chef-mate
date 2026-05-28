package com.plusmobileapps.chefmate.fakes

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.featureflag.FeatureFlag
import com.plusmobileapps.chefmate.featureflag.FeatureFlags
import com.plusmobileapps.chefmate.featureflag.impl.FeatureFlagRepositoryImpl
import com.plusmobileapps.chefmate.featureflag.testing.FakeFeatureFlags
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.StateFlow

/**
 * Replaces the production [FeatureFlagRepositoryImpl] in tests. Backed by [FakeFeatureFlags] so
 * tests can flip a flag with [set] before the screen renders.
 */
@SingleIn(AppScope::class)
@Inject
@ContributesBinding(scope = AppScope::class, replaces = [FeatureFlagRepositoryImpl::class])
class TestFeatureFlags(private val fake: FakeFeatureFlags = FakeFeatureFlags()) : FeatureFlags {

    override fun <T : Any> valueOf(flag: FeatureFlag<T>): StateFlow<T> = fake.valueOf(flag)

    override suspend fun refresh() = fake.refresh()

    fun <T : Any> set(flag: FeatureFlag<T>, value: T) = fake.set(flag, value)
}
