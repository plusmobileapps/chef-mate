package com.plusmobileapps.chefmate.recipe.core.impl.detail

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc.Output
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = RecipeDetailBloc.Factory::class,
)
class RecipeDetailBlocImpl(
    @Assisted context: BlocContext,
    @Assisted recipeId: Long,
    @Assisted private val output: Consumer<Output>
): RecipeDetailBloc, BlocContext by context {

    override val state: StateFlow<RecipeDetailBloc.Model> = MutableStateFlow(
        RecipeDetailBloc.Model(
            isLoading = false,
            recipe = Recipe.Empty,
        ),
    )

    override fun onEditClicked() {
        TODO("Not yet implemented")
    }

    override fun onDeleteClicked() {
        TODO("Not yet implemented")
    }

    override fun onFavoriteToggled() {
        TODO("Not yet implemented")
    }

    override fun onBackClicked() {
        output.onNext(Output.Finished)
    }
}