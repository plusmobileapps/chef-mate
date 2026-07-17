package com.plusmobileapps.chefmate.subscription.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.subscription.data.SubscriptionPackage
import com.plusmobileapps.chefmate.subscription.data.SubscriptionRepository
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class SubscriptionViewModel(
    @Main mainContext: CoroutineContext,
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        subscriptionRepository.state
            .onEach { subscription ->
                _state.update { current ->
                    val packages = subscription.offering?.packages.orEmpty()
                    current.copy(
                        isPremium = subscription.isPremium,
                        isLoading = subscription.isLoading,
                        packages = packages,
                        // Default the selection to the first package once one arrives, but never
                        // clobber an explicit user choice.
                        selectedPackageId = current.selectedPackageId ?: packages.firstOrNull()?.id,
                    )
                }
            }
            .launchIn(scope)
        scope.launch { subscriptionRepository.refresh() }
    }

    fun selectPackage(packageId: String) {
        _state.update { it.copy(selectedPackageId = packageId) }
    }

    fun purchase() {
        val current = _state.value
        val selected = current.packages.firstOrNull { it.id == current.selectedPackageId } ?: return
        scope.launch {
            _state.update { it.copy(isProcessing = true, showError = false) }
            val result = subscriptionRepository.purchase(selected)
            _state.update { it.copy(isProcessing = false, showError = result.isFailure) }
        }
    }

    fun restore() {
        scope.launch {
            _state.update { it.copy(isProcessing = true, showError = false) }
            val result = subscriptionRepository.restore()
            _state.update { it.copy(isProcessing = false, showError = result.isFailure) }
        }
    }

    fun dismissError() {
        _state.update { it.copy(showError = false) }
    }

    data class State(
        val isPremium: Boolean = false,
        val isLoading: Boolean = true,
        val packages: List<SubscriptionPackage> = emptyList(),
        val selectedPackageId: String? = null,
        val isProcessing: Boolean = false,
        val showError: Boolean = false,
    )
}
