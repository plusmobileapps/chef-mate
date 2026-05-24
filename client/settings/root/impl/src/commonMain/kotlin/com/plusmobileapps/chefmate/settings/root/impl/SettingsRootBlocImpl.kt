@file:OptIn(ExperimentalDecomposeApi::class, ExperimentalSerializationApi::class)

package com.plusmobileapps.chefmate.settings.root.impl

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.router.panels.PanelsNavigation
import com.arkivanov.decompose.router.panels.activateDetails
import com.arkivanov.decompose.router.panels.childPanels
import com.arkivanov.decompose.router.panels.dismissDetails
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderBloc
import com.plusmobileapps.chefmate.settings.AppSettingsBloc
import com.plusmobileapps.chefmate.settings.root.SettingsRootBloc
import com.plusmobileapps.chefmate.settings.root.SettingsRootBloc.DetailsChild
import com.plusmobileapps.chefmate.settings.root.SettingsRootBloc.MainChild
import com.plusmobileapps.chefmate.settings.root.SettingsRootBloc.Output
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = SettingsRootBloc.Factory::class,
)
class SettingsRootBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    private val appSettingsFactory: AppSettingsBloc.Factory,
    private val bottomNavOrderFactory: BottomNavOrderBloc.Factory,
) : SettingsRootBloc, BlocContext by context {

    private val navigation = PanelsNavigation<MainConfig, DetailsConfig, Nothing>()

    override val panels: Value<ChildPanels<*, MainChild, *, DetailsChild, *, *>> =
        childPanels(
            source = navigation,
            // Skip state persistence so every fresh open of SettingsRoot honours `initialPanels`.
            // With persistence on, an old saved state (details = null) would override the default
            // below and the right pane would render empty.
            serializers = null,
            // Default the details slot to the first child entry (BottomNavOrder) so the
            // tablet/expanded layout opens with the right pane populated rather than empty.
            // On COMPACT the user lands on the detail and can press back to surface the master
            // list, matching the rest of the app's stack-pop UX.
            initialPanels = {
                Panels(main = MainConfig.AppSettings, details = DetailsConfig.BottomNavOrder)
            },
            key = "SettingsRootPanels",
            handleBackButton = true,
            mainFactory = { config, ctx ->
                when (config) {
                    MainConfig.AppSettings ->
                        MainChild.AppSettings(
                            appSettingsFactory.create(
                                context = ctx,
                                output = ::handleAppSettingsOutput,
                            )
                        )
                }
            },
            detailsFactory = { config, ctx ->
                when (config) {
                    DetailsConfig.BottomNavOrder ->
                        DetailsChild.BottomNavOrder(
                            bottomNavOrderFactory.create(
                                context = ctx,
                                output = ::handleBottomNavOrderOutput,
                            )
                        )
                }
            },
        )

    private fun handleAppSettingsOutput(o: AppSettingsBloc.Output) {
        when (o) {
            AppSettingsBloc.Output.Back -> output.onNext(Output.Back)
            AppSettingsBloc.Output.OpenBottomNavOrder ->
                navigation.activateDetails(DetailsConfig.BottomNavOrder)
        }
    }

    private fun handleBottomNavOrderOutput(o: BottomNavOrderBloc.Output) {
        when (o) {
            BottomNavOrderBloc.Output.Back -> navigation.dismissDetails()
        }
    }

    @Serializable
    private sealed class MainConfig {
        @Serializable data object AppSettings : MainConfig()
    }

    @Serializable
    private sealed class DetailsConfig {
        @Serializable data object BottomNavOrder : DetailsConfig()
    }
}
