package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.browser.BrowserPreferences
import com.plusmobileapps.chefmate.browser.BrowserSelectEngineBloc
import com.plusmobileapps.chefmate.browser.SearchEngine
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = BrowserSelectEngineBloc.Factory::class,
)
class BrowserSelectEngineBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<BrowserSelectEngineBloc.Output>,
    private val browserPreferences: BrowserPreferences,
) : BrowserSelectEngineBloc, BlocContext by context {

    override fun onEngineSelected(engine: SearchEngine) {
        browserPreferences.setDefaultSearchEngine(engine)
        output.onNext(BrowserSelectEngineBloc.Output.EngineSelected)
    }
}
