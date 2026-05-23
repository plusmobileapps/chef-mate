@file:Suppress("FunctionName")
@file:OptIn(ExperimentalDecomposeApi::class)

package com.plusmobileapps.chefmate.settings.root.impl

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderBloc
import com.plusmobileapps.chefmate.settings.AppSettingsBloc
import com.plusmobileapps.chefmate.settings.root.SettingsRootBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import dev.mokkery.mock
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import kotlin.test.Test

class SettingsRootBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<SettingsRootBloc.Output>()
    private var appSettingsOutput: Consumer<AppSettingsBloc.Output> = Consumer {}
    private var bottomNavOrderOutput: Consumer<BottomNavOrderBloc.Output> = Consumer {}

    private val bloc =
        SettingsRootBlocImpl(
            context = context,
            output = output,
            appSettingsFactory =
                AppSettingsBloc.Factory { _: BlocContext, o: Consumer<AppSettingsBloc.Output> ->
                    appSettingsOutput = o
                    mock()
                },
            bottomNavOrderFactory =
                BottomNavOrderBloc.Factory { _: BlocContext, o: Consumer<BottomNavOrderBloc.Output>
                    ->
                    bottomNavOrderOutput = o
                    mock()
                },
        )

    @Test
    fun initial_state_shows_main_only() {
        val panels = bloc.panels.value
        panels.main.instance should instanceOf<SettingsRootBloc.MainChild.AppSettings>()
        panels.details shouldBe null
    }

    @Test
    fun app_settings_back_emits_root_back() {
        appSettingsOutput.onNext(AppSettingsBloc.Output.Back)
        output.lastValue shouldBe SettingsRootBloc.Output.Back
    }

    @Test
    fun app_settings_open_bottom_nav_order_activates_details() {
        appSettingsOutput.onNext(AppSettingsBloc.Output.OpenBottomNavOrder)
        val details = bloc.panels.value.details
        details.shouldNotBeNull()
        details.instance should instanceOf<SettingsRootBloc.DetailsChild.BottomNavOrder>()
    }

    @Test
    fun bottom_nav_order_back_dismisses_details() {
        appSettingsOutput.onNext(AppSettingsBloc.Output.OpenBottomNavOrder)
        bloc.panels.value.details.shouldNotBeNull()

        bottomNavOrderOutput.onNext(BottomNavOrderBloc.Output.Back)

        bloc.panels.value.details shouldBe null
    }
}
