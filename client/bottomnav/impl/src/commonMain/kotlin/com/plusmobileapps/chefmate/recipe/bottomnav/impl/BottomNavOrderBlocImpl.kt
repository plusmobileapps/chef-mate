package com.plusmobileapps.chefmate.recipe.bottomnav.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderBloc.Output
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class BottomNavOrderBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: Provider<BottomNavOrderViewModel>,
) : BottomNavOrderBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(context: BlocContext, output: Consumer<Output>): BottomNavOrderBlocImpl
    }

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<BottomNavOrderBloc.Model> = viewModel.state

    override fun onMove(from: Int, to: Int) {
        viewModel.move(from, to)
    }

    override fun onSave() {
        viewModel.save()
        output.onNext(Output.Back)
    }

    override fun onBack() {
        output.onNext(Output.Back)
    }
}

@ContributesTo(AppScope::class)
interface BottomNavOrderBlocBindingModule {
    @Provides
    fun provideBottomNavOrderBlocFactory(
        factory: BottomNavOrderBlocImpl.ManagedFactory
    ): BottomNavOrderBloc.Factory = BottomNavOrderBloc.Factory { context, output ->
        factory.create(context, output)
    }
}
