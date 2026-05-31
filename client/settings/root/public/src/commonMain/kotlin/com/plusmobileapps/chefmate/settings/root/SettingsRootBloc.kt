package com.plusmobileapps.chefmate.settings.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderBloc
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc
import com.plusmobileapps.chefmate.settings.AppSettingsBloc
import com.plusmobileapps.chefmate.ui.BlocScreen

interface SettingsRootBloc : BackHandlerOwner, BackClickBloc, BlocScreen {
    val routerState: Value<ChildStack<*, Child>>

    sealed class Child : BlocScreen {
        data class AppSettings(val bloc: AppSettingsBloc) : Child(), BlocScreen by bloc

        data class BottomNavOrder(val bloc: BottomNavOrderBloc) : Child(), BlocScreen by bloc

        data class ImportRecipes(val bloc: ImportRecipesBloc) : Child(), BlocScreen by bloc
    }

    sealed class Output {
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): SettingsRootBloc
    }
}
