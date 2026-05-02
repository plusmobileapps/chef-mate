package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.browser.BrowserLandingBloc
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
class BrowserLandingBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<BrowserLandingBloc.Output>,
    viewModelFactory: Provider<BrowserLandingViewModel>,
) : BrowserLandingBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(
            context: BlocContext,
            output: Consumer<BrowserLandingBloc.Output>,
        ): BrowserLandingBlocImpl
    }

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<BrowserLandingBloc.Model> = viewModel.state

    override fun onSearchTextChanged(text: String) {
        viewModel.onSearchTextChanged(text)
    }

    override fun onFocusChanged(isFocused: Boolean) {
        viewModel.onFocusChanged(isFocused)
    }

    override fun onNavigate() {
        val url = viewModel.state.value.searchText
        if (url.isBlank()) return
        output.onNext(BrowserLandingBloc.Output.Navigate(url.toNavigationUrl()))
    }

    override fun onCancel() {
        output.onNext(BrowserLandingBloc.Output.Cancel)
    }

    private fun String.toNavigationUrl(): String {
        val trimmed = trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            !trimmed.contains(" ") && trimmed.contains(".") -> "https://$trimmed"
            else -> "https://www.google.com/search?q=${trimmed.replace(" ", "+")}"
        }
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
