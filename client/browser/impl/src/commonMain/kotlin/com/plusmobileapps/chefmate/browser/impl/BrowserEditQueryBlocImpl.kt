package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.browser.BrowserEditQueryBloc
import com.plusmobileapps.chefmate.browser.BrowserHistoryEntry
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class BrowserEditQueryBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<BrowserEditQueryBloc.Output>,
    @Assisted initialText: String,
    viewModelFactory: Provider<BrowserEditQueryViewModel>,
) : BrowserEditQueryBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(
            context: BlocContext,
            output: Consumer<BrowserEditQueryBloc.Output>,
            initialText: String,
        ): BrowserEditQueryBlocImpl
    }

    private val viewModel = instanceKeeper.getViewModel {
        viewModelFactory().also {
            if (initialText.isNotEmpty()) it.onSearchTextChanged(initialText)
        }
    }

    override val state: StateFlow<BrowserEditQueryBloc.Model> = viewModel.state

    override fun onSearchTextChanged(text: String) {
        viewModel.onSearchTextChanged(text)
    }

    override fun onNavigate() {
        val url = viewModel.state.value.searchText
        if (url.isBlank()) return
        output.onNext(BrowserEditQueryBloc.Output.Navigate(url.toNavigationUrl()))
    }

    override fun onCancel() {
        output.onNext(BrowserEditQueryBloc.Output.Cancel)
    }

    override fun onHistoryItemClicked(entry: BrowserHistoryEntry) {
        output.onNext(BrowserEditQueryBloc.Output.Navigate(entry.url))
    }

    override fun onHistoryItemDeleteClicked(entry: BrowserHistoryEntry) {
        viewModel.deleteHistoryEntry(entry)
    }
}

@ContributesTo(AppScope::class)
interface BrowserEditQueryBlocBindingModule {
    @Provides
    fun provideBrowserEditQueryBlocFactory(
        factory: BrowserEditQueryBlocImpl.ManagedFactory
    ): BrowserEditQueryBloc.Factory = BrowserEditQueryBloc.Factory { context, output, initialText ->
        factory.create(context, output, initialText)
    }
}
