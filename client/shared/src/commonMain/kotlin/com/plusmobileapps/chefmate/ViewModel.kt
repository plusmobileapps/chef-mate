package com.plusmobileapps.chefmate

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class ViewModel(mainContext: CoroutineContext) {
    protected val scope = CoroutineScope(mainContext + SupervisorJob())

    open fun onCleared() {
        scope.cancel()
    }
}
