package com.plusmobileapps.chefmate.onboarding.demo

import android.app.Application
import dev.zacsweers.metro.createGraphFactory

/**
 * Creates the demo Metro graph once for the whole process — mirrors the production `MyApplication`.
 * Building it here (instead of in [MainActivity.onCreate]) means it survives configuration changes
 * like rotation rather than being recreated each time the activity is.
 */
class OnboardingDemoApplication : Application() {
    lateinit var component: OnboardingDemoComponent
        private set

    override fun onCreate() {
        super.onCreate()
        component = createGraphFactory<OnboardingDemoComponent.Factory>().create()
    }
}
