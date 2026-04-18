package com.plusmobileapps.chefmate.recipe.core.addmeal

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.meal.data.MealType
import kotlinx.coroutines.flow.StateFlow

interface AddToMealPlanBloc : BackClickBloc {
    val state: StateFlow<Model>

    fun onDateSelected(date: String)

    fun onMealTypeSelected(mealType: MealType)

    fun onSaveClicked()

    data class Model(
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val selectedDate: String = "",
        val selectedMealType: MealType = MealType.DINNER,
        val recipeTitle: String = "",
    )

    sealed class Output {
        data object Finished : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            recipeId: Long,
            output: Consumer<Output>,
        ): AddToMealPlanBloc
    }
}
