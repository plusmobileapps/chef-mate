package com.plusmobileapps.chefmate.subscription.data.impl

/** Desktop/JVM has no app store, so there is nothing to configure. */
actual class SubscriptionInitializer actual constructor() {
    actual fun initialize(apiKey: String, debug: Boolean) = Unit
}
