package com.plusmobileapps.chefmate.recipe.core.impl.addmeal

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import com.plusmobileapps.chefmate.meal.data.MealPlanRepository
import com.plusmobileapps.chefmate.util.DateTimeUtil
import com.russhwolf.settings.Settings
import com.russhwolf.settings.string
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
    settings: Settings,
) : ViewModel(mainContext) {

    private var lastSelectedDatePref by settings.string(KEY_LAST_SELECTED_DATE, "")

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var observeJob: Job? = null

    init {
        val today = dateTimeUtil.today()
        val initialDate = resolveInitialDate(today)
        val firstOfMonth = LocalDate(initialDate.year, initialDate.month, 1)
        val mealsForDay = emptyList<MealPlanItem>()
        _state.update {
            it.copy(
                firstDayOfMonth = firstOfMonth,
                monthLabel = buildMonthLabel(firstOfMonth),
                selectedDate = initialDate,
                formattedSelectedDate = dateTimeUtil.formatMediumDate(initialDate),
                selectedDayMeals = mealsForDay,
            )
        }
        observeMeals()
    }

    private fun resolveInitialDate(today: LocalDate): LocalDate {
        val saved = lastSelectedDatePref
        if (saved.isNotEmpty()) {
            try {
                val parsed = LocalDate.parse(saved)
                if (parsed >= today) return parsed
            } catch (_: Exception) {}
        }
        return today
    }

    fun onDaySelected(date: LocalDate) {
        lastSelectedDatePref = date.toString()
        _state.update { current ->
            val mealsForDay = current.allMeals.filter { it.date == date.toString() }
            current.copy(
                selectedDate = date,
                formattedSelectedDate = dateTimeUtil.formatMediumDate(date),
                selectedDayMeals = mealsForDay,
            )
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
                formattedSelectedDate = "",
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
                formattedSelectedDate = "",
                selectedDayMeals = emptyList(),
            )
        }
        observeMeals()
    }

    fun onTodayClicked() {
        val today = dateTimeUtil.today()
        val firstOfMonth = LocalDate(today.year, today.month, 1)
        lastSelectedDatePref = today.toString()
        _state.update {
            it.copy(
                firstDayOfMonth = firstOfMonth,
                monthLabel = buildMonthLabel(firstOfMonth),
                selectedDate = today,
                formattedSelectedDate = dateTimeUtil.formatMediumDate(today),
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
        val formattedSelectedDate: String = "",
        val selectedDayMeals: List<MealPlanItem> = emptyList(),
        val allMeals: List<MealPlanItem> = emptyList(),
    )

    companion object {
        private const val KEY_LAST_SELECTED_DATE = "choose_date_last_selected"
    }
}
