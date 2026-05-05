package com.plusmobileapps.chefmate.cook.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.cook.WhatsCookingBloc
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class WhatsCookingBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<WhatsCookingBloc.Output>,
    viewModelFactory: Provider<WhatsCookingViewModel>,
) : WhatsCookingBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(
            context: BlocContext,
            output: Consumer<WhatsCookingBloc.Output>,
        ): WhatsCookingBlocImpl
    }

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<WhatsCookingBloc.Model> =
        viewModel.state.mapState { vm ->
            WhatsCookingBloc.Model(
                recipes = vm.recipes,
                isSelectMode = vm.isSelectMode,
                selectedRecipeIds = vm.selectedRecipeIds,
            )
        }

    override fun onRecipeClicked(recipeId: Long) {
        output.onNext(WhatsCookingBloc.Output.RecipeSelected(recipeId))
    }

    override fun onSelectModeToggled() {
        viewModel.toggleSelectMode()
    }

    override fun onSelectionToggled(recipeId: Long) {
        viewModel.toggleSelection(recipeId)
    }

    override fun onDeleteSelectedClicked() {
        viewModel.deleteSelected()
    }

    override fun onCloseClicked() {
        output.onNext(WhatsCookingBloc.Output.Closed)
    }
}

@ContributesTo(AppScope::class)
interface WhatsCookingBlocBindingModule {
    @Provides
    fun provideWhatsCookingBlocFactory(
        factory: WhatsCookingBlocImpl.ManagedFactory
    ): WhatsCookingBloc.Factory = WhatsCookingBloc.Factory { context, output ->
        factory.create(context, output)
    }
}
