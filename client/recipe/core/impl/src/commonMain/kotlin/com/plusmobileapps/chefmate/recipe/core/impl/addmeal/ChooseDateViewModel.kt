package com.plusmobileapps.chefmate.recipe.core.impl.addmeal

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import com.plusmobileapps.chefmate.meal.data.MealPlanRepository
import com.plusmobileapps.chefmate.util.DateTimeUtil
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

@Inject
class ChooseDateViewModel(
    @Main mainContext: CoroutineContext,
    private val mealPlanRepository: MealPlanRepository,
    private val dateTimeUtil: DateTimeUtil,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var observeJob: Job? = null

    init {
        val today = dateTimeUtil.today()
        val firstOfMonth = LocalDate(today.year, today.month, 1)
        _state.update {
            it.copy(firstDayOfMonth = firstOfMonth, monthLabel = buildMonthLabel(firstOfMonth))
        }
        observeMeals()
    }

    fun onDaySelected(date: LocalDate) {
        _state.update { current ->
            val mealsForDay = current.allMeals.filter { it.date == date.toString() }
            current.copy(selectedDate = date, selectedDayMeals = mealsForDay)
        }
    }

    fun onPreviousMonth() {
        val current = _state.value
        val newFirst = current.firstDayOfMonth.minus(1, DateTimeUnit.MONTH)
        _state.update {
            it.copy(
                firstDayOfMonth = newFirst,
                monthLabel = buildMonthLabel(newFirst),
                selectedDate = null,
                selectedDayMeals = emptyList(),
            )
        }
        observeMeals()
    }

    fun onNextMonth() {
        val current = _state.value
        val newFirst = current.firstDayOfMonth.plus(1, DateTimeUnit.MONTH)
        _state.update {
            it.copy(
                firstDayOfMonth = newFirst,
                monthLabel = buildMonthLabel(newFirst),
                selectedDate = null,
                selectedDayMeals = emptyList(),
            )
        }
        observeMeals()
    }

    private fun observeMeals() {
        observeJob?.cancel()
        observeJob = scope.launch {
            val first = _state.value.firstDayOfMonth
            val last = first.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
            mealPlanRepository.getMealsByDateRange(first.toString(), last.toString()).collect {
                meals ->
                val daysWithMeals = meals.map { it.date }.toSet()
                _state.update { current ->
                    val selectedDayMeals =
                        current.selectedDate?.let { date ->
                            meals.filter { it.date == date.toString() }
                        } ?: emptyList()
                    current.copy(
                        allMeals = meals,
                        daysWithMeals = daysWithMeals,
                        selectedDayMeals = selectedDayMeals,
                    )
                }
            }
        }
    }

    private fun buildMonthLabel(date: LocalDate): String {
        val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$monthName ${date.year}"
    }

    data class State(
        val firstDayOfMonth: LocalDate = LocalDate(2000, 1, 1),
        val daysWithMeals: Set<String> = emptySet(),
        val monthLabel: String = "",
        val selectedDate: LocalDate? = null,
        val selectedDayMeals: List<MealPlanItem> = emptyList(),
        val allMeals: List<MealPlanItem> = emptyList(),
    )
}
