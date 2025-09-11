package com.plusmobileapps.chefmate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

abstract class ViewModel(
    mainContext: CoroutineContext,
) {
    protected val scope = CoroutineScope(mainContext + SupervisorJob())

    open fun onCleared() {
        scope.cancel()
    }
}
