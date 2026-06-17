package com.plusmobileapps.chefmate.settings

import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.coroutines.flow.StateFlow

interface AppSettingsBloc : BackHandlerOwner, ComposeScreen {
    val state: StateFlow<Model>

    fun onBack()

    fun onHistoryEnabledChanged(enabled: Boolean)

    fun onClearHistoryClicked()

    fun onClearHistoryConfirmed()

    fun onClearHistoryDismissed()

    fun onBottomNavOrderClicked()

    fun onImportRecipesClicked()

    fun onExportRecipesClicked()

    fun onRecipeCategoriesClicked()

    data class Model(
        val isHistoryEnabled: Boolean = true,
        val showClearHistoryDialog: Boolean = false,
    )

    sealed class Output {
        data object Back : Output()

        data object OpenBottomNavOrder : Output()

        data object OpenImportRecipes : Output()

        data object OpenExportRecipes : Output()

        data object OpenRecipeCategories : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): AppSettingsBloc
    }
}
