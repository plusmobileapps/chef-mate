package com.plusmobileapps.chefmate.subscription.data.testing

import com.plusmobileapps.chefmate.subscription.data.SubscriptionOffering
import com.plusmobileapps.chefmate.subscription.data.SubscriptionPackage
import com.plusmobileapps.chefmate.subscription.data.SubscriptionRepository
import com.plusmobileapps.chefmate.subscription.data.SubscriptionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * In-memory [SubscriptionRepository] for tests and previews. Drive [state] directly with the
 * setters, or let [purchase]/[restore] flip the user to premium via [purchaseResult].
 */
class FakeSubscriptionRepository(
    initialState: SubscriptionState = SubscriptionState(isLoading = false)
) : SubscriptionRepository {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<SubscriptionState> = _state

    /** Result returned by [refresh]; failures leave [state] untouched. */
    var refreshResult: Result<Unit> = Result.success(Unit)

    /** Result returned by [purchase]/[restore]; a success flips [state] to premium. */
    var purchaseResult: Result<Unit> = Result.success(Unit)

    var purchaseCallCount: Int = 0
        private set

    var lastPurchasedPackage: SubscriptionPackage? = null
        private set

    var restoreCallCount: Int = 0
        private set

    fun setPremium(isPremium: Boolean) {
        _state.value = _state.value.copy(isPremium = isPremium, isLoading = false)
    }

    fun setOffering(offering: SubscriptionOffering?) {
        _state.value = _state.value.copy(offering = offering, isLoading = false)
    }

    fun setState(state: SubscriptionState) {
        _state.value = state
    }

    override suspend fun refresh(): Result<Unit> = refreshResult

    override suspend fun purchase(subscriptionPackage: SubscriptionPackage): Result<Unit> {
        purchaseCallCount += 1
        lastPurchasedPackage = subscriptionPackage
        return purchaseResult.onSuccess { setPremium(true) }
    }

    override suspend fun restore(): Result<Unit> {
        restoreCallCount += 1
        return purchaseResult.onSuccess { setPremium(true) }
    }
}
