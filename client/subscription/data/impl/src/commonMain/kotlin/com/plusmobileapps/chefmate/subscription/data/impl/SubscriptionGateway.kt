package com.plusmobileapps.chefmate.subscription.data.impl

import com.plusmobileapps.chefmate.subscription.data.SubscriptionOffering

/**
 * Thin platform boundary over the store SDK. The Android/iOS actual talks to RevenueCat; the JVM
 * actual is a no-op. Kept as an interface (rather than an `expect class`) so
 * [createSubscriptionGateway] can hand [SubscriptionRepositoryImpl] a fake in `commonTest`.
 */
internal interface SubscriptionGateway {
    /** The current purchasable offering, or null when none is available. */
    suspend fun currentOffering(): SubscriptionOffering?

    /** Whether the premium entitlement is currently active for the store account. */
    suspend fun isPremiumActive(): Boolean

    /**
     * Runs the store purchase flow for the package with [packageId] (resolved against the current
     * offering) and returns whether premium is active afterward. Throws on failure/cancellation.
     */
    suspend fun purchase(packageId: String): Boolean

    /** Restores prior purchases and returns whether premium is active afterward. */
    suspend fun restore(): Boolean
}

/** Platform factory: RevenueCat-backed on Android/iOS, a no-op on JVM. */
internal expect fun createSubscriptionGateway(): SubscriptionGateway
