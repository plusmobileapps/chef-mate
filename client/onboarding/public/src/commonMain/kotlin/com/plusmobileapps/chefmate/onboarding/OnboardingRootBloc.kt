package com.plusmobileapps.chefmate.onboarding

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.BlocScreen

/**
 * Navigation BLoC that drives the first-run onboarding flow. It owns a Decompose router that walks
 * the user from the [WelcomeBloc] through to the final [StartCookingBloc]. When the user finishes,
 * it marks onboarding as completed and emits [Output.Finished] so the root can load the rest of the
 * app.
 *
 * This is currently a skeleton with only the welcome and start-cooking screens; additional steps
 * will be inserted between them in follow-up work.
 */
interface OnboardingRootBloc : BackHandlerOwner, BackClickBloc, BlocScreen {
    val routerState: Value<ChildStack<*, Child>>

    sealed class Child {

        abstract val bloc: BlocScreen

        data class Welcome(override val bloc: WelcomeBloc) : Child()

        data class StartCooking(override val bloc: StartCookingBloc) : Child()
    }

    sealed class Output {
        /** The user reached the end of onboarding; the root should load the main app. */
        data object Finished : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): OnboardingRootBloc
    }
}
