package com.plusmobileapps.chefmate.subscription.data

/**
 * Snapshot of the user's subscription status plus the offering that can be purchased.
 *
 * @property isPremium whether the premium entitlement is currently active.
 * @property isLoading true until the first successful (or failed) refresh completes; lets the
 *   paywall show a spinner instead of an empty state on cold start.
 * @property offering the current purchasable offering, or null when none is available (e.g. the
 *   no-op desktop impl, an offline start, or a misconfigured dashboard).
 */
data class SubscriptionState(
    val isPremium: Boolean = false,
    val isLoading: Boolean = true,
    val offering: SubscriptionOffering? = null,
)

/** A named group of purchasable [packages], mirroring a RevenueCat Offering. */
data class SubscriptionOffering(val identifier: String, val packages: List<SubscriptionPackage>)

/**
 * A single purchasable package. Wraps just the fields the paywall renders so the UI never touches a
 * platform SDK type.
 *
 * @property id the RevenueCat package identifier, used to resolve the package again at purchase.
 * @property title the localized store product title.
 * @property description the localized store product description.
 * @property priceFormatted the localized, currency-formatted price string (e.g. "$4.99").
 */
data class SubscriptionPackage(
    val id: String,
    val title: String,
    val description: String,
    val priceFormatted: String,
)
