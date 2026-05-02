package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.browser.BrowserLandingBloc
import com.plusmobileapps.chefmate.di.Main
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
class BrowserLandingViewModel(@Main mainContext: CoroutineContext) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(BrowserLandingBloc.Model())
    val state: StateFlow<BrowserLandingBloc.Model> = _state.asStateFlow()

    fun onSearchTextChanged(text: String) {
        _state.value = _state.value.copy(searchText = text)
    }
}
