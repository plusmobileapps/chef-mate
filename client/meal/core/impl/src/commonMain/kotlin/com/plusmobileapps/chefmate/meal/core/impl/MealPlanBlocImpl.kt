package com.plusmobileapps.chefmate.meal.core.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.meal.core.MealPlanBloc
import com.plusmobileapps.chefmate.meal.core.MealPlanBloc.Output
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class MealPlanBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: Provider<MealPlanViewModel>,
) : MealPlanBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(context: BlocContext, output: Consumer<Output>): MealPlanBlocImpl
    }

    private val viewModel: MealPlanViewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<MealPlanBloc.Model> =
        viewModel.state.mapState {
            MealPlanBloc.Model(
                isLoading = it.isLoading,
                viewMode = it.viewMode,
                dateLabel = viewModel.getDateLabel(),
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
                mealToDelete = it.mealToDelete,
            )
        }

    override fun onViewModeToggled() {
        viewModel.toggleViewMode()
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
}

@ContributesTo(AppScope::class)
interface MealPlanBlocBindingModule {
    @Provides
    fun provideMealPlanBlocFactory(factory: MealPlanBlocImpl.ManagedFactory): MealPlanBloc.Factory =
        MealPlanBloc.Factory { context, output ->
            factory.create(context, output)
        }
}
