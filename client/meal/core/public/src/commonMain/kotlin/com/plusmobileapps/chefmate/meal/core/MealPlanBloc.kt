package com.plusmobileapps.chefmate.meal.core

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import kotlinx.coroutines.flow.StateFlow

interface MealPlanBloc {
    val state: StateFlow<Model>

    fun onViewModeToggled()

    fun onPreviousClicked()

    fun onNextClicked()

    fun onMealClicked(item: MealPlanItem)

    fun onDeleteMealClicked(item: MealPlanItem)

    data class Model(
        val isLoading: Boolean = true,
        val viewMode: ViewMode = ViewMode.DAY,
        val dateLabel: String = "",
        val dayMeals: DayMeals? = null,
        val weekMeals: List<DayGroup>? = null,
    )

    data class DayMeals(
        val breakfast: List<MealPlanItem> = emptyList(),
        val lunch: List<MealPlanItem> = emptyList(),
        val dinner: List<MealPlanItem> = emptyList(),
        val snacks: List<MealPlanItem> = emptyList(),
    )

    data class DayGroup(val dateLabel: String, val meals: List<MealPlanItem>)

    enum class ViewMode {
        DAY,
        WEEK,
    }

    sealed class Output {
        data class OpenRecipe(val recipeId: Long) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): MealPlanBloc
    }
}
