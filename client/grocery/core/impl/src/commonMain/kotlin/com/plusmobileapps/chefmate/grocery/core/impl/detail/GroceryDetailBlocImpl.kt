package com.plusmobileapps.chefmate.grocery.core.impl.detail

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailBloc.Output
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.mapState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AssistedInject
class GroceryDetailBlocImpl(
    @Assisted context: BlocContext,
    @Assisted id: Long,
    @Assisted private val output: Consumer<Output>,
    repository: GroceryRepository,
) : GroceryDetailBloc,
    BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(
            context: BlocContext,
            id: Long,
            output: Consumer<Output>,
        ): GroceryDetailBlocImpl
    }

    private val scope = createScope()

    private val viewModel =
        instanceKeeper.getViewModel {
            GroceryDetailViewModel(
                id = id,
                mainContext = mainContext,
                repository = repository,
            )
        }

    override val models: StateFlow<GroceryDetailBloc.Model> =
        viewModel.state.mapState {
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

@ContributesTo(AppScope::class)
interface GroceryDetailBlocBindingModule {
    @Provides
    fun provideGroceryDetailBlocFactory(factory: GroceryDetailBlocImpl.ManagedFactory): GroceryDetailBloc.Factory =
        GroceryDetailBloc.Factory { context, id, output -> factory.create(context, id, output) }
}
