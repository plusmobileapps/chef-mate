package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.OnboardingRepository
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.chefmate.root.RootBloc
import com.plusmobileapps.chefmate.sync.SyncCoordinator
import com.plusmobileapps.chefmate.toast.ToastService
import com.russhwolf.settings.Settings

interface ApplicationComponent {
    val rootBlocFactory: RootBloc.Factory

    /**
     * Builds a recipe stack outside the root stack. Desktop uses this to give each detached recipe
     * window its own bloc tree; on the other targets nothing reaches for it.
     */
    val recipeRootBlocFactory: RecipeRootBloc.Factory

    val authenticationRepository: AuthenticationRepository
    val onboardingRepository: OnboardingRepository
    val settings: Settings
    val toastService: ToastService

    /**
     * Reconciles every repository on demand. Desktop drives this from window focus and a periodic
     * tick, since a process that stays up for days has no launch to fall back on.
     */
    val syncCoordinator: SyncCoordinator
}
