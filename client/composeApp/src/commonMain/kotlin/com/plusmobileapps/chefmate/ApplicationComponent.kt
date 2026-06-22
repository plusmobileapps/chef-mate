package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.OnboardingRepository
import com.plusmobileapps.chefmate.root.RootBloc
import com.plusmobileapps.chefmate.toast.ToastService
import com.russhwolf.settings.Settings

interface ApplicationComponent {
    val rootBlocFactory: RootBloc.Factory
    val authenticationRepository: AuthenticationRepository
    val onboardingRepository: OnboardingRepository
    val settings: Settings
    val toastService: ToastService
}
