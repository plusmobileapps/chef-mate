@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.subscription.impl

import com.plusmobileapps.chefmate.subscription.data.SubscriptionOffering
import com.plusmobileapps.chefmate.subscription.data.SubscriptionPackage
import com.plusmobileapps.chefmate.subscription.data.SubscriptionState
import com.plusmobileapps.chefmate.subscription.data.testing.FakeSubscriptionRepository
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class SubscriptionViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val monthly = SubscriptionPackage("monthly", "Monthly", "Billed monthly", "$4.99")
    private val yearly = SubscriptionPackage("yearly", "Yearly", "Billed yearly", "$39.99")
    private val offering = SubscriptionOffering("default", listOf(monthly, yearly))

    private fun newViewModel(repo: FakeSubscriptionRepository) =
        SubscriptionViewModel(mainContext = dispatcher, subscriptionRepository = repo)

    @Test
    fun refresh_on_init_populates_packages_and_defaults_selection() =
        runTest(dispatcher) {
            val repo =
                FakeSubscriptionRepository(
                    SubscriptionState(offering = offering, isLoading = false)
                )

            val vm = newViewModel(repo)

            vm.state.value.packages shouldBe listOf(monthly, yearly)
            vm.state.value.selectedPackageId shouldBe monthly.id
            vm.state.value.isLoading shouldBe false
        }

    @Test
    fun selectPackage_updates_selection() =
        runTest(dispatcher) {
            val repo =
                FakeSubscriptionRepository(
                    SubscriptionState(offering = offering, isLoading = false)
                )
            val vm = newViewModel(repo)

            vm.selectPackage(yearly.id)

            vm.state.value.selectedPackageId shouldBe yearly.id
        }

    @Test
    fun purchase_selected_package_flips_to_premium() =
        runTest(dispatcher) {
            val repo =
                FakeSubscriptionRepository(
                    SubscriptionState(offering = offering, isLoading = false)
                )
            val vm = newViewModel(repo)
            vm.selectPackage(yearly.id)

            vm.purchase()

            repo.lastPurchasedPackage shouldBe yearly
            vm.state.value.isPremium shouldBe true
            vm.state.value.isProcessing shouldBe false
            vm.state.value.showError shouldBe false
        }

    @Test
    fun purchase_failure_sets_error_flag() =
        runTest(dispatcher) {
            val repo =
                FakeSubscriptionRepository(
                    SubscriptionState(offering = offering, isLoading = false)
                )
            repo.purchaseResult = Result.failure(IllegalStateException("boom"))
            val vm = newViewModel(repo)

            vm.purchase()

            vm.state.value.showError shouldBe true
            vm.state.value.isProcessing shouldBe false
            vm.state.value.isPremium shouldBe false
        }

    @Test
    fun dismissError_clears_flag() =
        runTest(dispatcher) {
            val repo =
                FakeSubscriptionRepository(
                    SubscriptionState(offering = offering, isLoading = false)
                )
            repo.purchaseResult = Result.failure(IllegalStateException("boom"))
            val vm = newViewModel(repo)
            vm.purchase()

            vm.dismissError()

            vm.state.value.showError shouldBe false
        }
}
