@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.settings.root.impl

import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderBloc
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc
import com.plusmobileapps.chefmate.settings.AppSettingsBloc
import com.plusmobileapps.chefmate.settings.root.SettingsRootBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import dev.mokkery.mock
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import kotlin.test.Test

class SettingsRootBlocImplTest {
    val context = TestBlocContext.create()

    var appSettingsOutput: Consumer<AppSettingsBloc.Output> = Consumer {}
    var bottomNavOrderOutput: Consumer<BottomNavOrderBloc.Output> = Consumer {}
    var importRecipesOutput: Consumer<ImportRecipesBloc.Output> = Consumer {}

    var rootOutput: SettingsRootBloc.Output? = null

    val bloc =
        SettingsRootBlocImpl(
            context = context,
            output = { rootOutput = it },
            appSettings = { _, output ->
                appSettingsOutput = output
                mock()
            },
            bottomNavOrder = { _, output ->
                bottomNavOrderOutput = output
                mock()
            },
            importRecipes = { _, output ->
                importRecipesOutput = output
                mock()
            },
        )

    fun SettingsRootBloc.instance(): SettingsRootBloc.Child = routerState.value.active.instance

    @Test
    fun When_initialized_Then_app_settings_is_shown() {
        bloc.instance() should instanceOf<SettingsRootBloc.Child.AppSettings>()
        bloc.routerState.value.backStack.size shouldBe 0
    }

    @Test
    fun When_app_settings_opens_bottom_nav_order_Then_bottom_nav_order_is_shown() {
        appSettingsOutput.onNext(AppSettingsBloc.Output.OpenBottomNavOrder)
        bloc.instance() should instanceOf<SettingsRootBloc.Child.BottomNavOrder>()
        bloc.routerState.value.backStack.size shouldBe 1
    }

    @Test
    fun When_app_settings_opens_import_recipes_Then_import_recipes_is_shown() {
        appSettingsOutput.onNext(AppSettingsBloc.Output.OpenImportRecipes)
        bloc.instance() should instanceOf<SettingsRootBloc.Child.ImportRecipes>()
        bloc.routerState.value.backStack.size shouldBe 1
    }

    @Test
    fun Given_bottom_nav_order_When_it_outputs_back_Then_app_settings_is_shown() {
        appSettingsOutput.onNext(AppSettingsBloc.Output.OpenBottomNavOrder)
        bottomNavOrderOutput.onNext(BottomNavOrderBloc.Output.Back)
        bloc.instance() should instanceOf<SettingsRootBloc.Child.AppSettings>()
    }

    @Test
    fun Given_import_recipes_When_it_outputs_back_Then_app_settings_is_shown() {
        appSettingsOutput.onNext(AppSettingsBloc.Output.OpenImportRecipes)
        importRecipesOutput.onNext(ImportRecipesBloc.Output.Back)
        bloc.instance() should instanceOf<SettingsRootBloc.Child.AppSettings>()
    }

    @Test
    fun When_app_settings_outputs_back_Then_root_back_is_emitted() {
        appSettingsOutput.onNext(AppSettingsBloc.Output.Back)
        rootOutput shouldBe SettingsRootBloc.Output.Back
    }
}
