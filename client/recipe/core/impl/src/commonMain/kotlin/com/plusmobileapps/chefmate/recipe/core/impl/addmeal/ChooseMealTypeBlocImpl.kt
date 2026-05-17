package com.plusmobileapps.chefmate.recipe.core.impl.addmeal

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.recipe.core.addmeal.ChooseMealTypeBloc
import com.plusmobileapps.chefmate.recipe.core.addmeal.ChooseMealTypeBloc.Output
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = ChooseMealTypeBloc.Factory::class,
)
class ChooseMealTypeBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val recipeId: Long,
    @Assisted private val date: String,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: ChooseMealTypeViewModel.Factory,
) : ChooseMealTypeBloc, BlocContext by context {

    private val scope = createScope()

    private val viewModel: ChooseMealTypeViewModel = instanceKeeper.getViewModel {
        viewModelFactory.create(recipeId, date)
    }

    override val state: StateFlow<ChooseMealTypeBloc.Model> =
        viewModel.state.mapState {
            ChooseMealTypeBloc.Model(
                selectedMealType = it.selectedMealType,
                isSaving = it.isSaving,
                recipeTitle = it.recipeTitle,
                selectedDate = it.selectedDate,
            )
        }

    init {
        scope.launch {
            viewModel.output.collect {
                when (it) {
                    ChooseMealTypeViewModel.Output.Finished -> output.onNext(Output.Finished)
                }
            }
        }
    }

    override fun onMealTypeSelected(mealType: MealType) {
        viewModel.onMealTypeSelected(mealType)
    }

    override fun onSaveClicked() {
        viewModel.save()
    }

    override fun onBackClicked() {
        output.onNext(Output.Back)
    }
}
