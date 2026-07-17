package com.plusmobileapps.chefmate.fakes

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.subscription.data.SubscriptionOffering
import com.plusmobileapps.chefmate.subscription.data.SubscriptionRepository
import com.plusmobileapps.chefmate.subscription.data.impl.SubscriptionRepositoryImpl
import com.plusmobileapps.chefmate.subscription.data.testing.FakeSubscriptionRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Replaces the production [SubscriptionRepositoryImpl] in tests. Defaults to a non-premium, loaded
 * state so the AI-chat gate is exercised by default; call [setPremium] to unlock premium flows.
 */
@SingleIn(AppScope::class)
@Inject
@ContributesBinding(scope = AppScope::class, replaces = [SubscriptionRepositoryImpl::class])
class TestSubscriptionRepository(
    private val fake: FakeSubscriptionRepository = FakeSubscriptionRepository()
) : SubscriptionRepository by fake {

    fun setPremium(isPremium: Boolean) = fake.setPremium(isPremium)

    fun setOffering(offering: SubscriptionOffering?) = fake.setOffering(offering)
}
