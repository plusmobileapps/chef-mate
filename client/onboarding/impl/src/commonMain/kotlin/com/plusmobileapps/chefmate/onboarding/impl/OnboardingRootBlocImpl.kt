@file:OptIn(DelicateDecomposeApi::class)

package com.plusmobileapps.chefmate.onboarding.impl

import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.OnboardingRepository
import com.plusmobileapps.chefmate.onboarding.CookModeBloc
import com.plusmobileapps.chefmate.onboarding.GroceryListBloc
import com.plusmobileapps.chefmate.onboarding.MealPlanningBloc
import com.plusmobileapps.chefmate.onboarding.OnboardingRootBloc
import com.plusmobileapps.chefmate.onboarding.OnboardingRootBloc.Output
import com.plusmobileapps.chefmate.onboarding.SaveRecipesBloc
import com.plusmobileapps.chefmate.onboarding.StartCookingBloc
import com.plusmobileapps.chefmate.onboarding.WelcomeBloc
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.serialization.Serializable

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = OnboardingRootBloc.Factory::class,
)
class OnboardingRootBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    private val onboardingRepository: OnboardingRepository,
    private val welcome: WelcomeBloc.Factory,
    private val saveRecipes: SaveRecipesBloc.Factory,
    private val cookMode: CookModeBloc.Factory,
    private val groceryList: GroceryListBloc.Factory,
    private val mealPlanning: MealPlanningBloc.Factory,
    private val startCooking: StartCookingBloc.Factory,
) : OnboardingRootBloc, BlocContext by context {

    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialStack = { listOf(Configuration.Welcome) },
            handleBackButton = true,
            key = "OnboardingRootRouter",
            childFactory = ::createChild,
        )

    override val routerState: Value<ChildStack<*, OnboardingRootBloc.Child>> = stack

    override fun onBackClicked() {
        navigation.pop()
    }

    private fun createChild(config: Configuration, context: BlocContext): OnboardingRootBloc.Child =
        when (config) {
            Configuration.Welcome ->
                OnboardingRootBloc.Child.Welcome(
                    bloc = welcome.create(context = context, output = ::handleWelcomeOutput)
                )

            Configuration.SaveRecipes ->
                OnboardingRootBloc.Child.SaveRecipes(
                    bloc = saveRecipes.create(context = context, output = ::handleSaveRecipesOutput)
                )

            Configuration.CookMode ->
                OnboardingRootBloc.Child.CookMode(
                    bloc = cookMode.create(context = context, output = ::handleCookModeOutput)
                )

            Configuration.GroceryList ->
                OnboardingRootBloc.Child.GroceryList(
                    bloc = groceryList.create(context = context, output = ::handleGroceryListOutput)
                )

            Configuration.MealPlanning ->
                OnboardingRootBloc.Child.MealPlanning(
                    bloc =
                        mealPlanning.create(context = context, output = ::handleMealPlanningOutput)
                )

            Configuration.StartCooking ->
                OnboardingRootBloc.Child.StartCooking(
                    bloc =
                        startCooking.create(context = context, output = ::handleStartCookingOutput)
                )
        }

    /** Brings the next step to the front of the onboarding stack. */
    private fun advanceTo(configuration: Configuration) {
        navigation.bringToFront(configuration)
    }

    private fun handleWelcomeOutput(output: WelcomeBloc.Output) {
        when (output) {
            WelcomeBloc.Output.GetStarted -> advanceTo(Configuration.SaveRecipes)
            // The root opens the auth flow; on success it tears down onboarding for us.
            WelcomeBloc.Output.SignIn -> this.output.onNext(Output.SignIn)
            // Skipping is the same as finishing: mark complete so it never shows again.
            WelcomeBloc.Output.Skip -> finishOnboarding()
        }
    }

    private fun handleSaveRecipesOutput(output: SaveRecipesBloc.Output) {
        when (output) {
            SaveRecipesBloc.Output.Next -> advanceTo(Configuration.CookMode)
        }
    }

    private fun handleCookModeOutput(output: CookModeBloc.Output) {
        when (output) {
            CookModeBloc.Output.Next -> advanceTo(Configuration.GroceryList)
        }
    }

    private fun handleGroceryListOutput(output: GroceryListBloc.Output) {
        when (output) {
            GroceryListBloc.Output.Next -> advanceTo(Configuration.MealPlanning)
        }
    }

    private fun handleMealPlanningOutput(output: MealPlanningBloc.Output) {
        when (output) {
            MealPlanningBloc.Output.Next -> advanceTo(Configuration.StartCooking)
        }
    }

    private fun handleStartCookingOutput(output: StartCookingBloc.Output) {
        when (output) {
            StartCookingBloc.Output.StartCooking -> finishOnboarding()
        }
    }

    /** Persist completion so the flow is never shown again, then hand control back to the root. */
    private fun finishOnboarding() {
        onboardingRepository.setOnboardingCompleted()
        output.onNext(Output.Finished)
    }

    @Serializable
    private sealed class Configuration {
        @Serializable data object Welcome : Configuration()

        @Serializable data object SaveRecipes : Configuration()

        @Serializable data object CookMode : Configuration()

        @Serializable data object GroceryList : Configuration()

        @Serializable data object MealPlanning : Configuration()

        @Serializable data object StartCooking : Configuration()
    }
}
