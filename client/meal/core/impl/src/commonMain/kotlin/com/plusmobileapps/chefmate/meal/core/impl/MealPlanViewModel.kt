package com.plusmobileapps.chefmate.meal.core.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.meal.core.MealPlanBloc
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import com.plusmobileapps.chefmate.meal.data.MealPlanRepository
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.text.FixedString
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
class MealPlanViewModel(
    @Main mainContext: CoroutineContext,
    private val repository: MealPlanRepository,
    private val dateTimeUtil: DateTimeUtil,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var observeJob: Job? = null

    init {
        val today = dateTimeUtil.today()
        _state.update { it.copy(currentDate = today, selectedMonthDay = today) }
        observeMeals()
    }

    fun selectViewMode(mode: MealPlanBloc.ViewMode) {
        _state.update { it.copy(viewMode = mode) }
        observeMeals()
    }

    fun goNext() {
        val current = _state.value
        val newDate =
            when (current.viewMode) {
                MealPlanBloc.ViewMode.DAY -> current.currentDate.plus(1, DateTimeUnit.DAY)
                MealPlanBloc.ViewMode.WEEK -> current.currentDate.plus(7, DateTimeUnit.DAY)
                MealPlanBloc.ViewMode.MONTH -> {
                    val firstOfMonth =
                        LocalDate(current.currentDate.year, current.currentDate.month, 1)
                    firstOfMonth.plus(1, DateTimeUnit.MONTH)
                }
            }
        val newSelectedMonthDay =
            if (current.viewMode == MealPlanBloc.ViewMode.MONTH) newDate
            else current.selectedMonthDay
        _state.update { it.copy(currentDate = newDate, selectedMonthDay = newSelectedMonthDay) }
        observeMeals()
    }

    fun goPrevious() {
        val current = _state.value
        val newDate =
            when (current.viewMode) {
                MealPlanBloc.ViewMode.DAY -> current.currentDate.minus(1, DateTimeUnit.DAY)
                MealPlanBloc.ViewMode.WEEK -> current.currentDate.minus(7, DateTimeUnit.DAY)
                MealPlanBloc.ViewMode.MONTH -> {
                    val firstOfMonth =
                        LocalDate(current.currentDate.year, current.currentDate.month, 1)
                    firstOfMonth.minus(1, DateTimeUnit.MONTH)
                }
            }
        val newSelectedMonthDay =
            if (current.viewMode == MealPlanBloc.ViewMode.MONTH) newDate
            else current.selectedMonthDay
        _state.update { it.copy(currentDate = newDate, selectedMonthDay = newSelectedMonthDay) }
        observeMeals()
    }

    fun onMonthDaySelected(date: LocalDate) {
        _state.update { it.copy(selectedMonthDay = date) }
    }

    fun requestRemoveMeal(item: MealPlanItem) {
        _state.update { it.copy(mealToDelete = item) }
    }

    fun confirmRemoveMeal() {
        val item = _state.value.mealToDelete ?: return
        _state.update { it.copy(mealToDelete = null) }
        scope.launch { repository.removeMeal(item.id) }
    }

    fun dismissRemoveMeal() {
        _state.update { it.copy(mealToDelete = null) }
    }

    fun onSyncClicked() {
        scope.launch {
            _state.update { it.copy(isSyncing = true) }
            try {
                repository.syncAllUnsynced()
            } finally {
                _state.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun getDateLabel(): String {
        val current = _state.value
        return when (current.viewMode) {
            MealPlanBloc.ViewMode.DAY -> dateTimeUtil.formatLocalDate(current.currentDate)
            MealPlanBloc.ViewMode.WEEK -> {
                val (start, end) = getWeekRange(current.currentDate)
                val startStr = dateTimeUtil.formatLocalDate(start)
                val endStr = dateTimeUtil.formatLocalDate(end)
                "$startStr - $endStr"
            }
            MealPlanBloc.ViewMode.MONTH -> {
                val monthName =
                    current.currentDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
                "$monthName ${current.currentDate.year}"
            }
        }
    }

    fun buildDayMeals(meals: List<MealPlanItem>): MealPlanBloc.DayMeals =
        MealPlanBloc.DayMeals(
            breakfast = meals.filter { it.mealType == MealType.BREAKFAST },
            lunch = meals.filter { it.mealType == MealType.LUNCH },
            dinner = meals.filter { it.mealType == MealType.DINNER },
            snacks = meals.filter { it.mealType == MealType.SNACKS },
        )

    fun buildWeekMeals(meals: List<MealPlanItem>): List<MealPlanBloc.DayGroup> {
        val (start, _) = getWeekRange(_state.value.currentDate)
        return (0..6).map { dayOffset ->
            val date = start.plus(dayOffset, DateTimeUnit.DAY)
            val dateStr = date.toString()
            val dayMeals = meals.filter { it.date == dateStr }
            MealPlanBloc.DayGroup(
                dateLabel = FixedString(dateTimeUtil.formatShortDayDate(date)),
                meals = dayMeals,
            )
        }
    }

    fun buildMonthModel(
        meals: List<MealPlanItem>,
        selectedDay: LocalDate,
    ): MealPlanBloc.MonthModel {
        val current = _state.value
        val firstDayOfMonth = LocalDate(current.currentDate.year, current.currentDate.month, 1)
        val daysWithMeals = meals.map { it.date }.toSet()
        val selectedDayMeals = meals.filter { it.date == selectedDay.toString() }
        return MealPlanBloc.MonthModel(
            firstDayOfMonth = firstDayOfMonth,
            selectedDay = selectedDay,
            daysWithMeals = daysWithMeals,
            selectedDayMeals = buildDayMeals(selectedDayMeals),
        )
    }

    private fun observeMeals() {
        observeJob?.cancel()
        observeJob = scope.launch {
            val current = _state.value
            val flow =
                when (current.viewMode) {
                    MealPlanBloc.ViewMode.DAY ->
                        repository.getMealsByDate(current.currentDate.toString())
                    MealPlanBloc.ViewMode.WEEK -> {
                        val (start, end) = getWeekRange(current.currentDate)
                        repository.getMealsByDateRange(start.toString(), end.toString())
                    }
                    MealPlanBloc.ViewMode.MONTH -> {
                        val (start, end) = getMonthRange(current.currentDate)
                        repository.getMealsByDateRange(start.toString(), end.toString())
                    }
                }
            flow.collect { meals -> _state.update { it.copy(isLoading = false, meals = meals) } }
        }
    }

    private fun getWeekRange(date: LocalDate): Pair<LocalDate, LocalDate> {
        val daysFromSunday = date.dayOfWeek.ordinal.let { if (it == 6) 0 else it + 1 }
        val start = date.minus(daysFromSunday, DateTimeUnit.DAY)
        val end = start.plus(6, DateTimeUnit.DAY)
        return start to end
    }

    private fun getMonthRange(date: LocalDate): Pair<LocalDate, LocalDate> {
        val start = LocalDate(date.year, date.month, 1)
        val end = start.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        return start to end
    }

    data class State(
        val isLoading: Boolean = true,
        val isSyncing: Boolean = false,
        val viewMode: MealPlanBloc.ViewMode = MealPlanBloc.ViewMode.DAY,
        val currentDate: LocalDate = LocalDate(2000, 1, 1),
        val selectedMonthDay: LocalDate = LocalDate(2000, 1, 1),
        val meals: List<MealPlanItem> = emptyList(),
        val mealToDelete: MealPlanItem? = null,
    )
}
