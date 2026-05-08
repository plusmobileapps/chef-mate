package com.plusmobileapps.chefmate.fakes

import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.auth.data.impl.SupabaseAuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
@ContributesBinding(scope = AppScope::class, replaces = [SupabaseAuthenticationRepository::class])
class TestAuthenticationRepository(
    private val fake: FakeAuthenticationRepository = FakeAuthenticationRepository()
) : AuthenticationRepository by fake {

    fun setState(state: AuthState) = fake.setState(state)

    fun setAuthenticated(user: ChefMateUser = FakeAuthenticationRepository.fakeUser()) =
        fake.setAuthenticated(user)
}
