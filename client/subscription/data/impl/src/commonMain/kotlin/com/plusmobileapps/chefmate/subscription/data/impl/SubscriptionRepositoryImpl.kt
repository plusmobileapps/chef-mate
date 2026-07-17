package com.plusmobileapps.chefmate.subscription.data.impl

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.subscription.data.SubscriptionPackage
import com.plusmobileapps.chefmate.subscription.data.SubscriptionRepository
import com.plusmobileapps.chefmate.subscription.data.SubscriptionState
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * The primary constructor takes the [SubscriptionGateway] so `commonTest` can inject a fake; the
 * [Inject]-annotated secondary constructor is the one Metro uses in production and builds the
 * platform gateway ([createSubscriptionGateway]) itself. This keeps [SubscriptionGateway] internal
 * to the module — nothing outside needs a DI binding for it.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SubscriptionRepositoryImpl
internal constructor(
    private val gateway: SubscriptionGateway,
    private val ioContext: CoroutineContext,
) : SubscriptionRepository {

    @Inject
    constructor(@IO ioContext: CoroutineContext) : this(createSubscriptionGateway(), ioContext)

    private val _state = MutableStateFlow(SubscriptionState())
    override val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    override suspend fun refresh(): Result<Unit> =
        withContext(ioContext) {
            runCatching {
                val premium = gateway.isPremiumActive()
                val offering = gateway.currentOffering()
                _state.update {
                    it.copy(isPremium = premium, isLoading = false, offering = offering)
                }
            }
        }

    override suspend fun purchase(subscriptionPackage: SubscriptionPackage): Result<Unit> =
        withContext(ioContext) {
            runCatching {
                val premium = gateway.purchase(subscriptionPackage.id)
                _state.update { it.copy(isPremium = premium, isLoading = false) }
            }
        }

    override suspend fun restore(): Result<Unit> =
        withContext(ioContext) {
            runCatching {
                val premium = gateway.restore()
                _state.update { it.copy(isPremium = premium, isLoading = false) }
            }
        }
}
