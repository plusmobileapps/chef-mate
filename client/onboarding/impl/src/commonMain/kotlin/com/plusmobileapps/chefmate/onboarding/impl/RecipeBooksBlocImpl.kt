package com.plusmobileapps.chefmate.onboarding.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.onboarding.RecipeBooksBloc
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = RecipeBooksBloc.Factory::class,
)
class RecipeBooksBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<RecipeBooksBloc.Output>,
) : RecipeBooksBloc, BlocContext by context {

    override fun onNextClicked() {
        output.onNext(RecipeBooksBloc.Output.Next)
    }
}
