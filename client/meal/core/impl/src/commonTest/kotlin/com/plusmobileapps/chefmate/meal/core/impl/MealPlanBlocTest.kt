@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.meal.core.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.meal.core.MealPlanBloc
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import com.plusmobileapps.chefmate.meal.data.MealPlanRepository
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.util.testing.FakeDateTimeUtil
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class MealPlanBlocTest {
    val context = TestBlocContext.Companion.create()
    val output = TestConsumer<MealPlanBloc.Output>()
    val mealsFlow = MutableSharedFlow<List<MealPlanItem>>()
    val fakeDate = LocalDate(2026, 4, 17)
    val dateTimeUtil = FakeDateTimeUtil(fakeToday = fakeDate)

    val repository: MealPlanRepository = mock {
        every { getMealsByDate("2026-04-17") } returns mealsFlow
        every { getMealsByDateRange("2026-04-13", "2026-04-19") } returns mealsFlow
    }

    val bloc =
        MealPlanBlocImpl(
            context = context,
            output = output,
            viewModelFactory = {
                MealPlanViewModel(
                    mainContext = context.mainContext,
                    repository = repository,
                    dateTimeUtil = dateTimeUtil,
                )
            },
        )

    @Test
    fun WHEN_meals_loaded_THEN_state_is_updated_with_day_meals() = runTest {
        bloc.state.test {
            awaitItem() // initial loading state

            val items =
                listOf(
                    MealPlanItem(
                        id = 1,
                        recipeId = 10,
                        recipeTitle = "Pancakes",
                        recipeImageUrl = null,
                        date = "2026-04-17",
                        mealType = MealType.BREAKFAST,
                    ),
                    MealPlanItem(
                        id = 2,
                        recipeId = 20,
                        recipeTitle = "Salad",
                        recipeImageUrl = null,
                        date = "2026-04-17",
                        mealType = MealType.LUNCH,
                    ),
                )
            mealsFlow.emit(items)

            val state = awaitItem()
            state.isLoading shouldBe false
            state.viewMode shouldBe MealPlanBloc.ViewMode.DAY
            state.dayMeals?.breakfast?.size shouldBe 1
            state.dayMeals?.lunch?.size shouldBe 1
            state.dayMeals?.dinner?.size shouldBe 0
        }
    }

    @Test
    fun WHEN_meal_clicked_THEN_output_open_recipe() = runTest {
        val item =
            MealPlanItem(
                id = 1,
                recipeId = 10,
                recipeTitle = "Pancakes",
                recipeImageUrl = null,
                date = "2026-04-17",
                mealType = MealType.BREAKFAST,
            )
        bloc.onMealClicked(item)
        output.lastValue shouldBe MealPlanBloc.Output.OpenRecipe(10)
    }

    @Test
    fun WHEN_delete_meal_clicked_THEN_state_shows_meal_to_delete() = runTest {
        val item =
            MealPlanItem(
                id = 1,
                recipeId = 10,
                recipeTitle = "Pancakes",
                recipeImageUrl = null,
                date = "2026-04-17",
                mealType = MealType.BREAKFAST,
            )
        bloc.onDeleteMealClicked(item)

        bloc.state.test { awaitItem().mealToDelete shouldBe item }
    }

    @Test
    fun WHEN_delete_meal_confirmed_THEN_meal_removed_and_dialog_dismissed() = runTest {
        val item =
            MealPlanItem(
                id = 1,
                recipeId = 10,
                recipeTitle = "Pancakes",
                recipeImageUrl = null,
                date = "2026-04-17",
                mealType = MealType.BREAKFAST,
            )
        everySuspend { repository.removeMeal(1) } returns Unit

        bloc.onDeleteMealClicked(item)
        bloc.onDeleteMealConfirmed()

        bloc.state.test { awaitItem().mealToDelete shouldBe null }
        verifySuspend { repository.removeMeal(1) }
    }

    @Test
    fun WHEN_delete_meal_dismissed_THEN_dialog_dismissed() = runTest {
        val item =
            MealPlanItem(
                id = 1,
                recipeId = 10,
                recipeTitle = "Pancakes",
                recipeImageUrl = null,
                date = "2026-04-17",
                mealType = MealType.BREAKFAST,
            )
        bloc.onDeleteMealClicked(item)
        bloc.onDeleteMealDismissed()

        bloc.state.test { awaitItem().mealToDelete shouldBe null }
    }

    @Test
    fun WHEN_view_mode_toggled_THEN_mode_switches_to_week() = runTest {
        // Toggling to WEEK triggers getMealsByDateRange for the current week
        every { repository.getMealsByDateRange("2026-04-12", "2026-04-18") } returns mealsFlow

        bloc.onViewModeToggled()

        bloc.state.test { awaitItem().viewMode shouldBe MealPlanBloc.ViewMode.WEEK }
    }

    @Test
    fun WHEN_next_clicked_in_day_mode_THEN_date_advances_one_day() = runTest {
        every { repository.getMealsByDate("2026-04-18") } returns mealsFlow

        bloc.onNextClicked()

        bloc.state.test {
            val state = awaitItem()
            state.dateLabel shouldBe
                FixedString(dateTimeUtil.formatLocalDate(LocalDate(2026, 4, 18)))
        }
    }

    @Test
    fun WHEN_previous_clicked_in_day_mode_THEN_date_goes_back_one_day() = runTest {
        every { repository.getMealsByDate("2026-04-16") } returns mealsFlow

        bloc.onPreviousClicked()

        bloc.state.test {
            val state = awaitItem()
            state.dateLabel shouldBe
                FixedString(dateTimeUtil.formatLocalDate(LocalDate(2026, 4, 16)))
        }
    }
}
