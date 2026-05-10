package com.plusmobileapps.chefmate

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import com.plusmobileapps.chefmate.di.AndroidApplicationComponent
import dev.zacsweers.metro.createGraphFactory

class MyApplication : Application() {
    lateinit var appComponent: AndroidApplicationComponent

    override fun onCreate() {
        super.onCreate()
        BugsnagInitializer(this).initialize(BuildConfig.BUGSNAG_API_KEY)
        appComponent = createGraphFactory<AndroidApplicationComponent.Factory>().create(this)
        registerActivityLifecycleCallbacks(CurrentActivityTracker(appComponent))
    }

    private class CurrentActivityTracker(private val appComponent: AndroidApplicationComponent) :
        ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            appComponent.currentActivityHolder.current = activity
        }

        override fun onActivityPaused(activity: Activity) {
            if (appComponent.currentActivityHolder.current === activity) {
                appComponent.currentActivityHolder.current = null
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

        override fun onActivityStarted(activity: Activity) = Unit

        override fun onActivityStopped(activity: Activity) = Unit

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

        override fun onActivityDestroyed(activity: Activity) = Unit
    }
}
