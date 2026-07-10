package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen

/**
 * Navigation BLoC that drives the first-run onboarding flow. It owns a Decompose router that walks
 * the user from the [WelcomeBloc] through a series of feature tours ([SaveRecipesBloc],
 * [CookModeBloc], [GroceryListBloc], [MealPlanningBloc]) to the final [StartCookingBloc]. When the
 * user finishes (or skips), it marks onboarding as completed and emits [Output.Finished] so the
 * root can load the rest of the app.
 *
 * From the welcome screen the user can also choose to sign in; that is surfaced as [Output.SignIn]
 * so the root can open the authentication flow. From the final [StartCookingBloc] step, the user
 * can also choose to sign up for an account, surfaced as [Output.SignUp].
 */
interface OnboardingRootBloc : BackHandlerOwner, BackClickBloc, ComposeScreen {
    val routerState: Value<ChildStack<*, Child>>

    /** Total number of steps in the flow, used to render the nav bar's progress indicator. */
    val totalSteps: Int

    @Composable
    override fun Content(modifier: Modifier) {
        OnboardingRootScreen(bloc = this, modifier = modifier)
    }

    /** Skip the rest of onboarding; equivalent to finishing it. Surfaced in the top nav bar. */
    fun onSkipClicked()

    sealed class Child {

        abstract val bloc: ComposeScreen

        data class Welcome(override val bloc: WelcomeBloc) : Child()

        data class SaveRecipes(override val bloc: SaveRecipesBloc) : Child()

        data class CookMode(override val bloc: CookModeBloc) : Child()

        data class GroceryList(override val bloc: GroceryListBloc) : Child()

        data class MealPlanning(override val bloc: MealPlanningBloc) : Child()

        data class StartCooking(override val bloc: StartCookingBloc) : Child()
    }

    sealed class Output {
        /**
         * The user reached the end of onboarding (or skipped); the root should load the main app.
         */
        data object Finished : Output()

        /** The user wants to sign in; the root should open the authentication flow. */
        data object SignIn : Output()

        /** The user wants to create an account; the root should open the sign-up flow. */
        data object SignUp : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): OnboardingRootBloc
    }
}
