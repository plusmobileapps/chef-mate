package com.plusmobileapps.chefmate.featureflag.impl

import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface FeatureFlagEvaluatorModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideEvaluator(): FeatureFlagEvaluator = FeatureFlagEvaluator()
}
