@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.onboarding.impl

import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.OnboardingRepository
import com.plusmobileapps.chefmate.onboarding.CookModeBloc
import com.plusmobileapps.chefmate.onboarding.GroceryListBloc
import com.plusmobileapps.chefmate.onboarding.MealPlanningBloc
import com.plusmobileapps.chefmate.onboarding.OnboardingRootBloc
import com.plusmobileapps.chefmate.onboarding.SaveRecipesBloc
import com.plusmobileapps.chefmate.onboarding.StartCookingBloc
import com.plusmobileapps.chefmate.onboarding.WelcomeBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.russhwolf.settings.MapSettings
import dev.mokkery.mock
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import kotlin.test.Test

class OnboardingRootBlocImplTest {
    val context = TestBlocContext.create()
    val onboardingRepository = OnboardingRepository(MapSettings())

    var welcomeOutput: Consumer<WelcomeBloc.Output> = Consumer {}
    var saveRecipesOutput: Consumer<SaveRecipesBloc.Output> = Consumer {}
    var cookModeOutput: Consumer<CookModeBloc.Output> = Consumer {}
    var groceryListOutput: Consumer<GroceryListBloc.Output> = Consumer {}
    var mealPlanningOutput: Consumer<MealPlanningBloc.Output> = Consumer {}
    var startCookingOutput: Consumer<StartCookingBloc.Output> = Consumer {}

    var rootOutput: OnboardingRootBloc.Output? = null

    val bloc =
        OnboardingRootBlocImpl(
            context = context,
            output = { rootOutput = it },
            onboardingRepository = onboardingRepository,
            welcome = { _, output ->
                welcomeOutput = output
                mock()
            },
            saveRecipes = { _, output ->
                saveRecipesOutput = output
                mock()
            },
            cookMode = { _, output ->
                cookModeOutput = output
                mock()
            },
            groceryList = { _, output ->
                groceryListOutput = output
                mock()
            },
            mealPlanning = { _, output ->
                mealPlanningOutput = output
                mock()
            },
            startCooking = { _, output ->
                startCookingOutput = output
                mock()
            },
        )

    fun OnboardingRootBloc.instance(): OnboardingRootBloc.Child = routerState.value.active.instance

    @Test
    fun When_initialized_Then_welcome_is_shown() {
        bloc.instance() should instanceOf<OnboardingRootBloc.Child.Welcome>()
        bloc.routerState.value.backStack.size shouldBe 0
    }

    @Test
    fun When_the_user_steps_through_the_tour_Then_each_screen_is_shown_in_order() {
        welcomeOutput.onNext(WelcomeBloc.Output.GetStarted)
        bloc.instance() should instanceOf<OnboardingRootBloc.Child.SaveRecipes>()

        saveRecipesOutput.onNext(SaveRecipesBloc.Output.Next)
        bloc.instance() should instanceOf<OnboardingRootBloc.Child.CookMode>()

        cookModeOutput.onNext(CookModeBloc.Output.Next)
        bloc.instance() should instanceOf<OnboardingRootBloc.Child.GroceryList>()

        groceryListOutput.onNext(GroceryListBloc.Output.Next)
        bloc.instance() should instanceOf<OnboardingRootBloc.Child.MealPlanning>()

        mealPlanningOutput.onNext(MealPlanningBloc.Output.Next)
        bloc.instance() should instanceOf<OnboardingRootBloc.Child.StartCooking>()
    }

    @Test
    fun When_start_cooking_clicked_Then_onboarding_is_completed_and_finished_emitted() {
        welcomeOutput.onNext(WelcomeBloc.Output.GetStarted)
        saveRecipesOutput.onNext(SaveRecipesBloc.Output.Next)
        cookModeOutput.onNext(CookModeBloc.Output.Next)
        groceryListOutput.onNext(GroceryListBloc.Output.Next)
        mealPlanningOutput.onNext(MealPlanningBloc.Output.Next)

        startCookingOutput.onNext(StartCookingBloc.Output.StartCooking)

        onboardingRepository.hasCompletedOnboarding shouldBe true
        rootOutput shouldBe OnboardingRootBloc.Output.Finished
    }

    @Test
    fun When_skip_clicked_Then_onboarding_is_completed_and_finished_emitted() {
        // Skip lives in the shared nav bar and can be tapped from any step.
        welcomeOutput.onNext(WelcomeBloc.Output.GetStarted)

        bloc.onSkipClicked()

        onboardingRepository.hasCompletedOnboarding shouldBe true
        rootOutput shouldBe OnboardingRootBloc.Output.Finished
    }

    @Test
    fun When_back_clicked_Then_returns_to_the_previous_step() {
        welcomeOutput.onNext(WelcomeBloc.Output.GetStarted)
        bloc.instance() should instanceOf<OnboardingRootBloc.Child.SaveRecipes>()

        bloc.onBackClicked()

        bloc.instance() should instanceOf<OnboardingRootBloc.Child.Welcome>()
    }

    @Test
    fun When_welcome_outputs_sign_in_Then_sign_in_emitted_without_completing() {
        welcomeOutput.onNext(WelcomeBloc.Output.SignIn)

        rootOutput shouldBe OnboardingRootBloc.Output.SignIn
        // Completion is deferred to the root once auth actually succeeds.
        onboardingRepository.hasCompletedOnboarding shouldBe false
    }

    @Test
    fun When_start_cooking_outputs_sign_up_Then_sign_up_emitted_without_completing() {
        welcomeOutput.onNext(WelcomeBloc.Output.GetStarted)
        saveRecipesOutput.onNext(SaveRecipesBloc.Output.Next)
        cookModeOutput.onNext(CookModeBloc.Output.Next)
        groceryListOutput.onNext(GroceryListBloc.Output.Next)
        mealPlanningOutput.onNext(MealPlanningBloc.Output.Next)

        startCookingOutput.onNext(StartCookingBloc.Output.SignUp)

        rootOutput shouldBe OnboardingRootBloc.Output.SignUp
        // Completion is deferred to the root once auth actually succeeds.
        onboardingRepository.hasCompletedOnboarding shouldBe false
    }
}
