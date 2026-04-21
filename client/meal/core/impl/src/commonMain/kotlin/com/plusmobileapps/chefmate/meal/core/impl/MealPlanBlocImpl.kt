package com.plusmobileapps.chefmate.meal.core.impl

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.meal.core.MealPlanBloc
import com.plusmobileapps.chefmate.meal.core.MealPlanBloc.Output
import com.plusmobileapps.chefmate.meal.core.addmealsheet.AddMealSheetBloc
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import com.plusmobileapps.chefmate.text.FixedString
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@AssistedInject
class MealPlanBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: Provider<MealPlanViewModel>,
    private val addMealSheetFactory: AddMealSheetBloc.Factory,
) : MealPlanBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(context: BlocContext, output: Consumer<Output>): MealPlanBlocImpl
    }

    private val viewModel: MealPlanViewModel = instanceKeeper.getViewModel { viewModelFactory() }

    private val sheetNavigation = SlotNavigation<SheetConfig>()
    private val sheetRouter =
        childSlot(
            source = sheetNavigation,
            serializer = SheetConfig.serializer(),
            key = "MealPlanBloc_Sheet",
            childFactory = ::createSheet,
        )

    override val childSlot: Value<ChildSlot<*, MealPlanBloc.Sheet>> = sheetRouter

    override val state: StateFlow<MealPlanBloc.Model> =
        viewModel.state.mapState {
            MealPlanBloc.Model(
                isLoading = it.isLoading,
                viewMode = it.viewMode,
                dateLabel = FixedString(viewModel.getDateLabel()),
                dayMeals =
                    if (it.viewMode == MealPlanBloc.ViewMode.DAY) {
                        viewModel.buildDayMeals(it.meals)
                    } else {
                        null
                    },
                weekMeals =
                    if (it.viewMode == MealPlanBloc.ViewMode.WEEK) {
                        viewModel.buildWeekMeals(it.meals)
                    } else {
                        null
                    },
                monthModel =
                    if (it.viewMode == MealPlanBloc.ViewMode.MONTH) {
                        viewModel.buildMonthModel(it.meals, it.selectedMonthDay)
                    } else {
                        null
                    },
                mealToDelete = it.mealToDelete,
            )
        }

    override fun onViewModeSelected(mode: MealPlanBloc.ViewMode) {
        viewModel.selectViewMode(mode)
    }

    override fun onMonthDaySelected(date: LocalDate) {
        viewModel.onMonthDaySelected(date)
    }

    override fun onPreviousClicked() {
        viewModel.goPrevious()
    }

    override fun onNextClicked() {
        viewModel.goNext()
    }

    override fun onMealClicked(item: MealPlanItem) {
        output.onNext(Output.OpenRecipe(item.recipeId))
    }

    override fun onDeleteMealClicked(item: MealPlanItem) {
        viewModel.requestRemoveMeal(item)
    }

    override fun onDeleteMealConfirmed() {
        viewModel.confirmRemoveMeal()
    }

    override fun onDeleteMealDismissed() {
        viewModel.dismissRemoveMeal()
    }

    override fun onAddMealClicked() {
        sheetNavigation.activate(SheetConfig.AddMeal)
    }

    override fun onDismissSheet() {
        sheetNavigation.dismiss()
    }

    private fun createSheet(config: SheetConfig, context: BlocContext): MealPlanBloc.Sheet =
        when (config) {
            SheetConfig.AddMeal ->
                MealPlanBloc.Sheet.AddMeal(
                    bloc =
                        addMealSheetFactory.create(
                            context = context,
                            output = { sheetOutput ->
                                when (sheetOutput) {
                                    AddMealSheetBloc.Output.Finished -> sheetNavigation.dismiss()
                                }
                            },
                        )
                )
        }

    @Serializable
    sealed class SheetConfig {
        @Serializable data object AddMeal : SheetConfig()
    }
}

@ContributesTo(AppScope::class)
interface MealPlanBlocBindingModule {
    @Provides
    fun provideMealPlanBlocFactory(factory: MealPlanBlocImpl.ManagedFactory): MealPlanBloc.Factory =
        MealPlanBloc.Factory { context, output ->
            factory.create(context, output)
        }
}
