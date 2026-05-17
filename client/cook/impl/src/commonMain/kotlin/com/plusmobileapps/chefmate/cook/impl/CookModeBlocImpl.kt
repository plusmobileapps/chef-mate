package com.plusmobileapps.chefmate.cook.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.cook.CookModeBloc
import com.plusmobileapps.chefmate.cook.WhatsCookingBloc
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = CookModeBloc.Factory::class)
class CookModeBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val initialRecipeId: Long,
    @Assisted private val output: Consumer<CookModeBloc.Output>,
    private val viewModelFactory: CookModeViewModel.Factory,
    whatsCookingFactory: WhatsCookingBloc.Factory,
) : CookModeBloc, BlocContext by context {

    private val viewModel: CookModeViewModel = instanceKeeper.getViewModel {
        viewModelFactory.create(initialRecipeId)
    }

    override val whatsCookingBloc: WhatsCookingBloc =
        whatsCookingFactory.create(context = context, output = ::handleWhatsCookingOutput)

    override val state: StateFlow<CookModeBloc.Model> =
        viewModel.state.mapState { vm ->
            val active = vm.cookingRecipes.firstOrNull { it.id == vm.activeRecipeId }
            CookModeBloc.Model(
                isLoading = vm.isLoading,
                activeRecipe = active,
                activeSessions =
                    vm.cookingRecipes.map { recipe ->
                        CookModeBloc.Model.Chip(
                            recipeId = recipe.id,
                            title = recipe.title,
                            isActive = recipe.id == vm.activeRecipeId,
                        )
                    },
                layoutMode = vm.layoutMode,
                keepScreenOn = vm.keepScreenOn,
            )
        }

    override fun onCloseClicked() {
        output.onNext(CookModeBloc.Output.Finished)
    }

    override fun onRecipeChipClicked(recipeId: Long) {
        viewModel.selectRecipe(recipeId)
    }

    override fun onLayoutToggled() {
        viewModel.toggleLayout()
    }

    override fun onKeepScreenOnToggled() {
        viewModel.toggleKeepScreenOn()
    }

    override fun onBackClicked() {
        output.onNext(CookModeBloc.Output.Finished)
    }

    private fun handleWhatsCookingOutput(out: WhatsCookingBloc.Output) {
        when (out) {
            WhatsCookingBloc.Output.Closed -> {
                // Dismissal is handled at the screen level (sheet swipe / modal close button) —
                // nothing to do at the bloc layer.
            }
            is WhatsCookingBloc.Output.RecipeSelected -> viewModel.selectRecipe(out.recipeId)
        }
    }
}
