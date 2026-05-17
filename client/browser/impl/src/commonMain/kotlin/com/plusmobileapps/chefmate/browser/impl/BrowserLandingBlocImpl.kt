package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.browser.BrowserLandingBloc
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = BrowserLandingBloc.Factory::class,
)
class BrowserLandingBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<BrowserLandingBloc.Output>,
) : BrowserLandingBloc, BlocContext by context {

    override fun onSearchFieldFocused() {
        output.onNext(BrowserLandingBloc.Output.OpenEditQuery)
    }
}
