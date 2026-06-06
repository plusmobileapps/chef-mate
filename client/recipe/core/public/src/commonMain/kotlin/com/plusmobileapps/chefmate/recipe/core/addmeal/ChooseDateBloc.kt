package com.plusmobileapps.chefmate.recipe.core.addmeal

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

interface ChooseDateBloc : BackClickBloc, BlocScreen {
    val state: StateFlow<Model>

    fun onDaySelected(date: LocalDate)

    fun onNextClicked()

    fun onPreviousMonth()

    fun onNextMonth()

    fun onTodayClicked()

    data class Model(
        val firstDayOfMonth: LocalDate = LocalDate(2000, 1, 1),
        val daysWithMeals: Set<String> = emptySet(),
        val monthLabel: String = "",
        val selectedDate: LocalDate? = null,
        val formattedSelectedDate: String = "",
        val selectedDayMeals: ImmutableList<MealPlanItem> = persistentListOf(),
    )

    sealed class Output {
        data class DateSelected(val date: LocalDate) : Output()

        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): ChooseDateBloc
    }
}
