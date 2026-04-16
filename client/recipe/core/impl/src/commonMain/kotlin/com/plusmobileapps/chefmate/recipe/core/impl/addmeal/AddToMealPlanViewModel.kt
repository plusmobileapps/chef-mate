package com.plusmobileapps.chefmate.recipe.core.impl.addmeal

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.meal.data.MealPlanRepository
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@AssistedInject
class AddToMealPlanViewModel(
    @Assisted private val recipeId: Long,
    @Main mainContext: CoroutineContext,
    private val recipeRepository: RecipeRepository,
    private val mealPlanRepository: MealPlanRepository,
) : ViewModel(mainContext) {

    @AssistedFactory
    fun interface Factory {
        fun create(recipeId: Long): AddToMealPlanViewModel
    }

    private val _output = Channel<Output>(Channel.BUFFERED)
    val output: Flow<Output> = _output.receiveAsFlow()

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        _state.update { it.copy(selectedDate = today) }
        loadRecipe()
    }

    override fun onCleared() {
        super.onCleared()
        _output.close()
    }

    fun onDateSelected(date: String) {
        _state.update { it.copy(selectedDate = date) }
    }

    fun onMealTypeSelected(mealType: MealType) {
        _state.update { it.copy(selectedMealType = mealType) }
    }

    fun save() {
        val currentState = _state.value
        if (currentState.selectedDate.isEmpty() || currentState.isSaving) return
        _state.update { it.copy(isSaving = true) }
        scope.launch {
            mealPlanRepository.addMeal(
                recipeId = recipeId,
                date = currentState.selectedDate,
                mealType = currentState.selectedMealType,
            )
            _output.send(Output.Finished)
        }
    }

    private fun loadRecipe() {
        scope.launch {
            val recipe = recipeRepository.getRecipe(recipeId).first() ?: return@launch
            _state.update { it.copy(isLoading = false, recipeTitle = recipe.title) }
        }
    }

    data class State(
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val selectedDate: String = "",
        val selectedMealType: MealType = MealType.DINNER,
        val recipeTitle: String = "",
    )

    sealed class Output {
        data object Finished : Output()
    }
}
