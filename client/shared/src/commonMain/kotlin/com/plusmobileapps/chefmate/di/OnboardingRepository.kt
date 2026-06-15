package com.plusmobileapps.chefmate.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Tracks whether the user has completed the first-run onboarding flow. Persisted via
 * multiplatform-settings so the flow is only ever shown once per install. The root navigation reads
 * [hasCompletedOnboarding] when deciding the initial screen, and the onboarding flow calls
 * [setOnboardingCompleted] once the user reaches the end.
 */
@Inject
@SingleIn(AppScope::class)
class OnboardingRepository(settings: Settings) {
    private var completed by settings.boolean(KEY, defaultValue = false)

    val hasCompletedOnboarding: Boolean
        get() = completed

    fun setOnboardingCompleted() {
        completed = true
    }

    companion object {
        private const val KEY = "onboarding_completed"
    }
}
