package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.browser.BrowserLandingBloc
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@AssistedInject
class BrowserLandingBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<BrowserLandingBloc.Output>,
) : BrowserLandingBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(
            context: BlocContext,
            output: Consumer<BrowserLandingBloc.Output>,
        ): BrowserLandingBlocImpl
    }

    override fun onSearchFieldFocused() {
        output.onNext(BrowserLandingBloc.Output.OpenEditQuery)
    }
}

@ContributesTo(AppScope::class)
interface BrowserLandingBlocBindingModule {
    @Provides
    fun provideBrowserLandingBlocFactory(
        factory: BrowserLandingBlocImpl.ManagedFactory
    ): BrowserLandingBloc.Factory = BrowserLandingBloc.Factory { context, output ->
        factory.create(context, output)
    }
}
