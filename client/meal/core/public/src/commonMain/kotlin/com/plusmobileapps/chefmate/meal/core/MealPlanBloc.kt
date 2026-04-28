package com.plusmobileapps.chefmate.meal.core

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import com.plusmobileapps.chefmate.recipe.core.addmeal.MealPlannerRootBloc
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.TextData
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

interface MealPlanBloc {
    val state: StateFlow<Model>

    fun onViewModeSelected(mode: ViewMode)

    fun onPreviousClicked()

    fun onNextClicked()

    fun onMealClicked(item: MealPlanItem)

    fun onDeleteMealClicked(item: MealPlanItem)

    fun onDeleteMealConfirmed()

    fun onDeleteMealDismissed()

    fun onMonthDaySelected(date: LocalDate)

    fun onAddMealClicked()

    fun onSyncClicked()

    data class Model(
        val isLoading: Boolean = true,
        val isSyncing: Boolean = false,
        val viewMode: ViewMode = ViewMode.DAY,
        val dateLabel: TextData = FixedString(""),
        val dayMeals: DayMeals? = null,
        val weekMeals: List<DayGroup>? = null,
        val monthModel: MonthModel? = null,
        val mealToDelete: MealPlanItem? = null,
    )

    data class DayMeals(
        val breakfast: List<MealPlanItem> = emptyList(),
        val lunch: List<MealPlanItem> = emptyList(),
        val dinner: List<MealPlanItem> = emptyList(),
        val snacks: List<MealPlanItem> = emptyList(),
    )

    data class DayGroup(val dateLabel: TextData, val meals: List<MealPlanItem>)

    data class MonthModel(
        val firstDayOfMonth: LocalDate,
        val selectedDay: LocalDate,
        val daysWithMeals: Set<String>,
        val selectedDayMeals: DayMeals,
    )

    enum class ViewMode {
        DAY,
        WEEK,
        MONTH,
    }

    sealed class Output {
        data class OpenRecipe(val recipeId: Long) : Output()

        data class OpenMealPlanner(val props: MealPlannerRootBloc.Props) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): MealPlanBloc
    }
}
