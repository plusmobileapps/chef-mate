package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import com.plusmobileapps.chefmate.subscription.data.impl.SubscriptionInitializer

/** Called from `iOSApp.swift` at launch to configure RevenueCat with the iOS API key. */
fun initializeSubscriptions() {
    SubscriptionInitializer().initialize(BuildConfig.REVENUECAT_IOS_API_KEY, BuildConfig.IS_DEBUG)
}
