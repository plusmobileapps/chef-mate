package com.plusmobileapps.chefmate.subscription.data.impl

import com.plusmobileapps.chefmate.subscription.data.SubscriptionOffering

/**
 * Desktop/JVM has no app store, so there is nothing to purchase: the user is never premium and the
 * paywall renders its "unavailable" state. Mirrors the product decision that subscriptions are
 * mobile-only.
 */
internal actual fun createSubscriptionGateway(): SubscriptionGateway = NoOpSubscriptionGateway

private object NoOpSubscriptionGateway : SubscriptionGateway {
    override suspend fun currentOffering(): SubscriptionOffering? = null

    override suspend fun isPremiumActive(): Boolean = false

    override suspend fun purchase(packageId: String): Boolean =
        throw UnsupportedOperationException("Subscriptions are not available on this platform")

    override suspend fun restore(): Boolean = false
}
