package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.plusmobileapps.chefmate.recipe.bottomnav.impl.ui.previewBottomNavOrderBloc
import com.plusmobileapps.chefmate.settings.impl.ui.previewAppSettingsBloc
import com.plusmobileapps.chefmate.settings.root.SettingsRootBloc
import com.plusmobileapps.chefmate.settings.root.SettingsRootScreen
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

private fun settingsRootBloc(child: SettingsRootBloc.Child): SettingsRootBloc =
    object : SettingsRootBloc {
        override val backHandler: BackHandler = BackDispatcher()
        override val routerState: Value<ChildStack<*, SettingsRootBloc.Child>> =
            MutableValue(ChildStack(configuration = "config", instance = child))

        override fun onBackClicked() = Unit

        @Composable
        override fun Content(modifier: Modifier) {
            SettingsRootScreen(bloc = this, modifier = modifier)
        }
    }

private val appSettingsActive =
    settingsRootBloc(SettingsRootBloc.Child.AppSettings(previewAppSettingsBloc))

private val bottomNavOrderActive =
    settingsRootBloc(SettingsRootBloc.Child.BottomNavOrder(previewBottomNavOrderBloc))

@PreviewTest
@Preview(showBackground = true)
@Composable
fun SettingsRootAppSettingsLightScreenshot() {
    ChefMateTheme { SettingsRootScreen(bloc = appSettingsActive) }
}

@PreviewTest
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsRootAppSettingsDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { SettingsRootScreen(bloc = appSettingsActive) }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun SettingsRootBottomNavOrderLightScreenshot() {
    ChefMateTheme { SettingsRootScreen(bloc = bottomNavOrderActive) }
}

@PreviewTest
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsRootBottomNavOrderDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { SettingsRootScreen(bloc = bottomNavOrderActive) }
}
