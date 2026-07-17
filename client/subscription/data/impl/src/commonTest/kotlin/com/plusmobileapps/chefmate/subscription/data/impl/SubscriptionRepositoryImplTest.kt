@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.subscription.data.impl

import com.plusmobileapps.chefmate.subscription.data.SubscriptionOffering
import com.plusmobileapps.chefmate.subscription.data.SubscriptionPackage
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class SubscriptionRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun repository(gateway: SubscriptionGateway) =
        SubscriptionRepositoryImpl(gateway = gateway, ioContext = testDispatcher)

    @Test
    fun initial_state_is_loading_and_not_premium() {
        val repo = repository(FakeGateway())
        repo.state.value.isLoading shouldBe true
        repo.state.value.isPremium shouldBe false
    }

    @Test
    fun refresh_publishes_premium_and_offering() =
        runTest(testDispatcher) {
            val offering = SubscriptionOffering("default", listOf(premiumPackage))
            val repo = repository(FakeGateway(premium = true, offering = offering))

            val result = repo.refresh()

            result.isSuccess shouldBe true
            repo.state.value.isLoading shouldBe false
            repo.state.value.isPremium shouldBe true
            repo.state.value.offering shouldBe offering
        }

    @Test
    fun refresh_failure_leaves_state_untouched_and_returns_failure() =
        runTest(testDispatcher) {
            val repo = repository(FakeGateway(isPremiumError = IllegalStateException("boom")))

            val result = repo.refresh()

            result.isFailure shouldBe true
            repo.state.value.isLoading shouldBe true
        }

    @Test
    fun purchase_flips_state_to_premium() =
        runTest(testDispatcher) {
            val gateway = FakeGateway()
            val repo = repository(gateway)

            val result = repo.purchase(premiumPackage)

            result.isSuccess shouldBe true
            gateway.purchasedPackageId shouldBe premiumPackage.id
            repo.state.value.isPremium shouldBe true
        }

    @Test
    fun restore_flips_state_to_premium_when_gateway_reports_active() =
        runTest(testDispatcher) {
            val repo = repository(FakeGateway(premium = true))

            val result = repo.restore()

            result.isSuccess shouldBe true
            repo.state.value.isPremium shouldBe true
        }

    private val premiumPackage =
        SubscriptionPackage(
            id = "monthly",
            title = "Premium Monthly",
            description = "All premium features",
            priceFormatted = "$4.99",
        )

    private class FakeGateway(
        private val premium: Boolean = false,
        private val offering: SubscriptionOffering? = null,
        private val isPremiumError: Throwable? = null,
    ) : SubscriptionGateway {
        var purchasedPackageId: String? = null
            private set

        override suspend fun currentOffering(): SubscriptionOffering? = offering

        override suspend fun isPremiumActive(): Boolean {
            isPremiumError?.let { throw it }
            return premium
        }

        override suspend fun purchase(packageId: String): Boolean {
            purchasedPackageId = packageId
            return true
        }

        override suspend fun restore(): Boolean = premium
    }
}
