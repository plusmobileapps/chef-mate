package com.plusmobileapps.chefmate.subscription.data.impl

import com.plusmobileapps.chefmate.subscription.data.SubscriptionOffering
import com.plusmobileapps.chefmate.subscription.data.SubscriptionPackage
import com.plusmobileapps.chefmate.subscription.data.SubscriptionRepository
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.result.awaitCustomerInfoResult
import com.revenuecat.purchases.kmp.result.awaitOfferingsResult
import com.revenuecat.purchases.kmp.result.awaitPurchaseResult
import com.revenuecat.purchases.kmp.result.awaitRestoreResult

// NOTE: identical to the androidMain twin (SubscriptionGateway.android.kt). The purchases-kmp SDK
// exposes one common API for Android + iOS, but the default source-set hierarchy has no shared
// parent for the two, so the code is duplicated. Keep both files in sync.
internal actual fun createSubscriptionGateway(): SubscriptionGateway =
    RevenueCatSubscriptionGateway()

private class RevenueCatSubscriptionGateway : SubscriptionGateway {
    private val purchases: Purchases
        get() = Purchases.sharedInstance

    override suspend fun currentOffering(): SubscriptionOffering? =
        purchases.awaitOfferingsResult().getOrThrow().current?.toDomain()

    override suspend fun isPremiumActive(): Boolean =
        purchases.awaitCustomerInfoResult().getOrThrow().isPremium()

    override suspend fun purchase(packageId: String): Boolean {
        val offering =
            purchases.awaitOfferingsResult().getOrThrow().current
                ?: throw IllegalStateException("No current offering available")
        val packageToPurchase =
            offering.availablePackages.firstOrNull { it.identifier == packageId }
                ?: throw IllegalArgumentException("Package $packageId not in current offering")
        return purchases
            .awaitPurchaseResult(packageToPurchase = packageToPurchase)
            .getOrThrow()
            .customerInfo
            .isPremium()
    }

    override suspend fun restore(): Boolean =
        purchases.awaitRestoreResult().getOrThrow().isPremium()
}

private fun Offering.toDomain(): SubscriptionOffering =
    SubscriptionOffering(
        identifier = identifier,
        packages = availablePackages.map { it.toDomain() },
    )

private fun Package.toDomain(): SubscriptionPackage =
    SubscriptionPackage(
        id = identifier,
        title = storeProduct.title,
        description = storeProduct.localizedDescription.orEmpty(),
        priceFormatted = storeProduct.price.formatted,
    )

private fun CustomerInfo.isPremium(): Boolean =
    entitlements[SubscriptionRepository.PREMIUM_ENTITLEMENT_ID]?.isActive == true
