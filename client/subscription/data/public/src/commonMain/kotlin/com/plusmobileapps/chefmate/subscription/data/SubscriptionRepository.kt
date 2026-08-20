package com.plusmobileapps.chefmate.subscription.data

import kotlinx.coroutines.flow.StateFlow

/**
 * Source of truth for the user's premium subscription. Backed by RevenueCat on Android/iOS and a
 * no-op on every other target (see the `impl` module). Other features gate premium functionality
 * off [state] (currently the AI chat, gated in the root navigation), and integration tests should
 * depend on the `subscription:data:testing` fake rather than this contract's production impl.
 */
interface SubscriptionRepository {
    /** Current subscription status. Emits [SubscriptionState.Loading] until the first refresh. */
    val state: StateFlow<SubscriptionState>

    /** Re-fetches entitlements and the current offering. Safe to call repeatedly. */
    suspend fun refresh(): Result<Unit>

    /**
     * Launches the store purchase flow for [subscriptionPackage] and, on success, updates [state]
     * to reflect the newly-active entitlement.
     */
    suspend fun purchase(subscriptionPackage: SubscriptionPackage): Result<Unit>

    /** Restores prior purchases for the current store account and refreshes [state]. */
    suspend fun restore(): Result<Unit>

    companion object {
        /**
         * RevenueCat entitlement identifier that unlocks premium features. Must match the
         * entitlement configured in the RevenueCat dashboard.
         */
        const val PREMIUM_ENTITLEMENT_ID: String = "premium"
    }
}
