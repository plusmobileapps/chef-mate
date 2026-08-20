package com.plusmobileapps.chefmate

import android.app.Application
import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import com.plusmobileapps.chefmate.di.AndroidApplicationComponent
import com.plusmobileapps.chefmate.subscription.data.impl.SubscriptionInitializer
import dev.zacsweers.metro.createGraphFactory

class MyApplication : Application() {
    lateinit var appComponent: AndroidApplicationComponent

    override fun onCreate() {
        super.onCreate()
        BugsnagInitializer(this).initialize(BuildConfig.BUGSNAG_API_KEY)
        SubscriptionInitializer()
            .initialize(BuildConfig.REVENUECAT_ANDROID_API_KEY, BuildConfig.IS_DEBUG)
        appComponent = createGraphFactory<AndroidApplicationComponent.Factory>().create(this)
    }
}
