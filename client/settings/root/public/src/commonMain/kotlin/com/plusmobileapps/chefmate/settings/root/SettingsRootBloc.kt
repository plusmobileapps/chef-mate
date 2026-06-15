package com.plusmobileapps.chefmate.settings.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderBloc
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc
import com.plusmobileapps.chefmate.settings.AppSettingsBloc
import com.plusmobileapps.chefmate.ui.BlocScreen

interface SettingsRootBloc : BackHandlerOwner, BackClickBloc, BlocScreen {
    val routerState: Value<ChildStack<*, Child>>

    sealed class Child {

        abstract val bloc: BlocScreen

        data class AppSettings(override val bloc: AppSettingsBloc) : Child()

        data class BottomNavOrder(override val bloc: BottomNavOrderBloc) : Child()

        data class ExportRecipes(override val bloc: ExportRecipesBloc) : Child()

        data class ImportRecipes(override val bloc: ImportRecipesBloc) : Child()

        data class RecipeCategories(override val bloc: RecipeCategoriesBloc) : Child()
    }

    sealed class Output {
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): SettingsRootBloc
    }
}
