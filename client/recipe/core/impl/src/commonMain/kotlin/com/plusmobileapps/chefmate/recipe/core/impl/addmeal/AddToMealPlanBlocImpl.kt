package com.plusmobileapps.chefmate.recipe.core.impl.addmeal

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.recipe.core.addmeal.AddToMealPlanBloc
import com.plusmobileapps.chefmate.recipe.core.addmeal.AddToMealPlanBloc.Output
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AssistedInject
class AddToMealPlanBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val recipeId: Long,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: AddToMealPlanViewModel.Factory,
) : AddToMealPlanBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(
            context: BlocContext,
            recipeId: Long,
            output: Consumer<Output>,
        ): AddToMealPlanBlocImpl
    }

    private val scope = createScope()

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory.create(recipeId) }

    override val state: StateFlow<AddToMealPlanBloc.Model> =
        viewModel.state.mapState {
            AddToMealPlanBloc.Model(
                isLoading = it.isLoading,
                isSaving = it.isSaving,
                selectedDate = it.selectedDate,
                selectedMealType = it.selectedMealType,
                recipeTitle = it.recipeTitle,
            )
        }

    init {
        scope.launch {
            viewModel.output.collect {
                when (it) {
                    AddToMealPlanViewModel.Output.Finished -> {
                        output.onNext(Output.Finished)
                    }
                }
            }
        }
    }

    override fun onDateSelected(date: String) {
        viewModel.onDateSelected(date)
    }

    override fun onMealTypeSelected(mealType: MealType) {
        viewModel.onMealTypeSelected(mealType)
    }

    override fun onSaveClicked() {
        viewModel.save()
    }

    override fun onBackClicked() {
        output.onNext(Output.Finished)
    }
}

@ContributesTo(AppScope::class)
interface AddToMealPlanBlocBindingModule {
    @Provides
    fun provideAddToMealPlanBlocFactory(
        factory: AddToMealPlanBlocImpl.ManagedFactory
    ): AddToMealPlanBloc.Factory = AddToMealPlanBloc.Factory { context, recipeId, output ->
        factory.create(context, recipeId, output)
    }
}
