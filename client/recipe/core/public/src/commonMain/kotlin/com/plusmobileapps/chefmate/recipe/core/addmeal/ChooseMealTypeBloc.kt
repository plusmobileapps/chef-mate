package com.plusmobileapps.chefmate.recipe.core.addmeal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.coroutines.flow.StateFlow

interface ChooseMealTypeBloc : BackClickBloc, ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        ChooseMealTypeScreen(bloc = this, modifier = modifier)
    }

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
