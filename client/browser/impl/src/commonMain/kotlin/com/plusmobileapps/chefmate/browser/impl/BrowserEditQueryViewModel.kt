package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.browser.BrowserEditQueryBloc
import com.plusmobileapps.chefmate.di.Main
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
class BrowserEditQueryViewModel(@Main mainContext: CoroutineContext) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(BrowserEditQueryBloc.Model())
    val state: StateFlow<BrowserEditQueryBloc.Model> = _state.asStateFlow()

    fun onSearchTextChanged(text: String) {
        _state.value = _state.value.copy(searchText = text)
    }
}
