package com.plusmobileapps.chefmate.grocerylist.detail

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.grocerylist.GroceryRepository
import com.plusmobileapps.chefmate.grocerylist.detail.GroceryDetailBloc.Output
import com.plusmobileapps.chefmate.mapState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroceryDetailBlocImpl(
    context: BlocContext,
    id: Long,
    repository: GroceryRepository,
    private val output: Consumer<Output>,
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