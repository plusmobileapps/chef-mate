package com.plusmobileapps.chefmate.subscription.data.impl

/**
 * Configures the store SDK at app startup. Android/iOS configure RevenueCat with the platform API
 * key; every other target no-ops. Call exactly once, as early as possible (before any
 * `SubscriptionRepository` use), from each platform's entry point.
 *
 * Follows the same expect/actual class pattern as `BugsnagInitializer`. A blank [apiKey] is treated
 * as "not configured" and skips setup, keeping debug/desktop builds runnable without real keys.
 */
expect class SubscriptionInitializer() {
    fun initialize(apiKey: String, debug: Boolean)
}
