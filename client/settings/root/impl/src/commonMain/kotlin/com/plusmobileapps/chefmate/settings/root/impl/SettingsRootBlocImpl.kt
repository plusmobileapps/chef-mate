@file:OptIn(DelicateDecomposeApi::class)

package com.plusmobileapps.chefmate.settings.root.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderBloc
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc
import com.plusmobileapps.chefmate.settings.AppSettingsBloc
import com.plusmobileapps.chefmate.settings.root.SettingsRootBloc
import com.plusmobileapps.chefmate.settings.root.SettingsRootBloc.Output
import com.plusmobileapps.chefmate.settings.root.SettingsRootScreen
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.serialization.Serializable

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = SettingsRootBloc.Factory::class,
)
class SettingsRootBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    private val appSettings: AppSettingsBloc.Factory,
    private val bottomNavOrder: BottomNavOrderBloc.Factory,
    private val importRecipes: ImportRecipesBloc.Factory,
) : SettingsRootBloc, BlocContext by context {

    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialStack = { listOf(Configuration.AppSettings) },
            handleBackButton = true,
            key = "SettingsRootRouter",
            childFactory = ::createChild,
        )

    override val routerState: Value<ChildStack<*, SettingsRootBloc.Child>> = stack

    override fun onBackClicked() {
        navigation.pop()
    }

    @Composable
    override fun Content(modifier: Modifier) {
        SettingsRootScreen(bloc = this, modifier = modifier)
    }

    private fun createChild(config: Configuration, context: BlocContext): SettingsRootBloc.Child =
        when (config) {
            Configuration.AppSettings ->
                SettingsRootBloc.Child.AppSettings(
                    bloc = appSettings.create(context = context, output = ::handleAppSettingsOutput)
                )

            Configuration.BottomNavOrder ->
                SettingsRootBloc.Child.BottomNavOrder(
                    bloc =
                        bottomNavOrder.create(
                            context = context,
                            output = ::handleBottomNavOrderOutput,
                        )
                )

            Configuration.ImportRecipes ->
                SettingsRootBloc.Child.ImportRecipes(
                    bloc =
                        importRecipes.create(
                            context = context,
                            output = ::handleImportRecipesOutput,
                        )
                )
        }

    private fun handleAppSettingsOutput(output: AppSettingsBloc.Output) {
        when (output) {
            AppSettingsBloc.Output.Back -> this.output.onNext(Output.Back)
            AppSettingsBloc.Output.OpenBottomNavOrder ->
                navigation.bringToFront(Configuration.BottomNavOrder)
            AppSettingsBloc.Output.OpenImportRecipes ->
                navigation.bringToFront(Configuration.ImportRecipes)
        }
    }

    private fun handleBottomNavOrderOutput(output: BottomNavOrderBloc.Output) {
        when (output) {
            BottomNavOrderBloc.Output.Back -> navigation.pop()
        }
    }

    private fun handleImportRecipesOutput(output: ImportRecipesBloc.Output) {
        when (output) {
            ImportRecipesBloc.Output.Back -> navigation.pop()
        }
    }

    @Serializable
    private sealed class Configuration {
        @Serializable data object AppSettings : Configuration()

        @Serializable data object BottomNavOrder : Configuration()

        @Serializable data object ImportRecipes : Configuration()
    }
}
