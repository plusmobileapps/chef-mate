package com.plusmobileapps.chefmate.recipe.core.addmeal

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.coroutines.flow.StateFlow

interface ChooseMealTypeBloc : BackClickBloc, BlocScreen {
    val state: StateFlow<Model>

    fun onMealTypeSelected(mealType: MealType)

    fun onSaveClicked()

    data class Model(
        val selectedMealType: MealType = MealType.DINNER,
        val isSaving: Boolean = false,
        val recipeTitle: String = "",
        val selectedDate: String = "",
    )

    sealed class Output {
        data object Finished : Output()

        data object Back : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            recipeId: Long,
            date: String,
            output: Consumer<Output>,
        ): ChooseMealTypeBloc
    }
}
