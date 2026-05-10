package com.plusmobileapps.chefmate.auth.data.impl

import android.app.Activity
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Tracks the currently-resumed Activity so platform APIs that require an Activity context (notably
 * Credential Manager's credential-picker UI) can resolve one at call time. The host `MyApplication`
 * registers Activity lifecycle callbacks that drive [current].
 */
@Inject
@SingleIn(AppScope::class)
class CurrentActivityHolder {
    @Volatile var current: Activity? = null
}
