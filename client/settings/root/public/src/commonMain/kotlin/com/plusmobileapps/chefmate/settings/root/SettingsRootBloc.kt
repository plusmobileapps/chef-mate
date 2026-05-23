@file:OptIn(ExperimentalDecomposeApi::class)

package com.plusmobileapps.chefmate.settings.root

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderBloc
import com.plusmobileapps.chefmate.settings.AppSettingsBloc

interface SettingsRootBloc : BackHandlerOwner {
    val panels: Value<ChildPanels<*, MainChild, *, DetailsChild, *, *>>

    sealed class MainChild {
        data class AppSettings(val bloc: AppSettingsBloc) : MainChild()
    }

    sealed class DetailsChild {
        data class BottomNavOrder(val bloc: BottomNavOrderBloc) : DetailsChild()
    }

    sealed class Output {
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): SettingsRootBloc
    }
}
