package com.plusmobileapps.chefmate.recipe.core.impl.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = RecipeRootBloc.Factory::class,
)
class RecipeRootBlocImpl(
    @Assisted context: BlocContext,
    @Assisted props: RecipeRootBloc.Props,
    @Assisted output: Consumer<RecipeRootBloc.Output>,
) : RecipeRootBloc,
    BlocContext by context {
    override val routerState: Value<ChildStack<*, RecipeRootBloc.Child>>
        get() = TODO("Not yet implemented")
}
