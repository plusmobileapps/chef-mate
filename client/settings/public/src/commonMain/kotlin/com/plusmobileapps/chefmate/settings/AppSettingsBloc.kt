package com.plusmobileapps.chefmate.settings

import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import kotlinx.coroutines.flow.StateFlow

interface AppSettingsBloc : BackHandlerOwner {
    val state: StateFlow<Model>

    fun onBack()

    fun onHistoryEnabledChanged(enabled: Boolean)

    fun onClearHistoryClicked()

    fun onClearHistoryConfirmed()

    fun onClearHistoryDismissed()

    data class Model(
        val isHistoryEnabled: Boolean = true,
        val showClearHistoryDialog: Boolean = false,
    )

    sealed class Output {
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): AppSettingsBloc
    }
}
