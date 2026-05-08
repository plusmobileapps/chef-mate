package com.plusmobileapps.chefmate.di

import com.plusmobileapps.chefmate.ApplicationComponent
import com.plusmobileapps.chefmate.fakes.FakeDatabase
import com.plusmobileapps.chefmate.fakes.TestAuthenticationRepository

interface TestApplicationComponent : ApplicationComponent {
    val fakeDatabase: FakeDatabase
    val testAuthenticationRepository: TestAuthenticationRepository
}

expect fun createTestApplicationComponent(): TestApplicationComponent
