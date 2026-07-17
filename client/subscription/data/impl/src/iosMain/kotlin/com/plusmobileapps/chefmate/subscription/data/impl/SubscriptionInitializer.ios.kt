package com.plusmobileapps.chefmate.subscription.data.impl

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

// Twin of SubscriptionInitializer.android.kt — keep in sync.
actual class SubscriptionInitializer actual constructor() {
    actual fun initialize(apiKey: String, debug: Boolean) {
        if (apiKey.isBlank()) return
        Purchases.logLevel = if (debug) LogLevel.DEBUG else LogLevel.ERROR
        Purchases.configure(PurchasesConfiguration.Builder(apiKey).build())
    }
}
