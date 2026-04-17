@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.recipe.core.impl.addmeal

import app.cash.turbine.test
import com.plusmobileapps.chefmate.meal.data.MealPlanRepository
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.recipe.core.addmeal.AddToMealPlanBloc
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import com.plusmobileapps.chefmate.util.testing.FakeDateTimeUtil
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class AddToMealPlanBlocTest {
    val context = TestBlocContext.Companion.create()
    val output = TestConsumer<AddToMealPlanBloc.Output>()
    val fakeDate = LocalDate(2026, 4, 17)
    val dateTimeUtil = FakeDateTimeUtil(fakeToday = fakeDate)
    val recipe = Recipe.Empty.copy(id = 1, title = "Pancakes")

    val recipeRepository: RecipeRepository = mock {
        everySuspend { getRecipe(1) } returns flowOf(recipe)
    }
    val mealPlanRepository: MealPlanRepository = mock()

    val bloc =
        AddToMealPlanBlocImpl(
            context = context,
            recipeId = 1,
            output = output,
            viewModelFactory =
                AddToMealPlanViewModel.Factory { recipeId ->
                    AddToMealPlanViewModel(
                        recipeId = recipeId,
                        mainContext = context.mainContext,
                        recipeRepository = recipeRepository,
                        mealPlanRepository = mealPlanRepository,
                        dateTimeUtil = dateTimeUtil,
                    )
                },
        )

    @Test
    fun WHEN_loaded_THEN_state_has_recipe_title_and_todays_date() = runTest {
        bloc.state.test {
            // The ViewModel init sets the date and loads the recipe.
            // With UnconfinedTestDispatcher, both happen before we collect.
            val state = awaitItem()
            state.recipeTitle shouldBe "Pancakes"
            state.selectedDate shouldBe "2026-04-17"
            state.selectedMealType shouldBe MealType.DINNER
        }
    }

    @Test
    fun WHEN_date_selected_THEN_state_updates() = runTest {
        bloc.onDateSelected("2026-04-20")
        bloc.state.test { awaitItem().selectedDate shouldBe "2026-04-20" }
    }

    @Test
    fun WHEN_meal_type_selected_THEN_state_updates() = runTest {
        bloc.onMealTypeSelected(MealType.BREAKFAST)
        bloc.state.test { awaitItem().selectedMealType shouldBe MealType.BREAKFAST }
    }

    @Test
    fun WHEN_save_clicked_THEN_meal_added_and_output_finished() = runTest {
        everySuspend {
            mealPlanRepository.addMeal(
                recipeId = 1,
                date = "2026-04-17",
                mealType = MealType.DINNER,
            )
        } returns Unit

        bloc.onSaveClicked()

        verifySuspend {
            mealPlanRepository.addMeal(
                recipeId = 1,
                date = "2026-04-17",
                mealType = MealType.DINNER,
            )
        }
        output.lastValue shouldBe AddToMealPlanBloc.Output.Finished
    }

    @Test
    fun WHEN_back_clicked_THEN_output_finished() = runTest {
        bloc.onBackClicked()
        output.lastValue shouldBe AddToMealPlanBloc.Output.Finished
    }
}
