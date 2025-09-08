package com.plusmobileapps.chefmate.grocerylist.detail

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.grocerylist.GroceryRepository
import com.plusmobileapps.chefmate.grocerylist.detail.GroceryDetailBloc.Output
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = GroceryDetailBloc.Factory::class,
)
class GroceryDetailBlocImpl(
    @Assisted context: BlocContext,
    @Assisted id: Long,
    @Assisted private val output: Consumer<Output>,
    repository: GroceryRepository,
) : GroceryDetailBloc, BlocContext by context {

    private val scope = createScope()

    private val viewModel = instanceKeeper.getViewModel {
        GroceryDetailViewModel(
            id = id,
            mainContext = mainContext,
            repository = repository,
        )
    }

    override val models: StateFlow<GroceryDetailBloc.Model> = viewModel.state.mapState {
        if (it.isLoading) {
            GroceryDetailBloc.Model.Loading
        } else {
            GroceryDetailBloc.Model.Loaded(it.groceryItem)
        }
    }

    init {
        scope.launch {
            viewModel.outputs.collect {
                when (it) {
                    GroceryDetailViewModel.Output.Finished -> output.onNext(Output.Finished)
                }
            }
        }
    }

    override fun onGroceryNameChanged(name: String) {
        viewModel.onGroceryNameChanged(name)
    }

    override fun onGroceryCheckedChanged(isChecked: Boolean) {
        viewModel.onGroceryCheckedChanged(isChecked)
    }

    override fun onSaveClicked() {
        viewModel.save()
    }

    override fun onBackClicked() {
        output.onNext(Output.Finished)
    }
}