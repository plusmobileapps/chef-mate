@file:OptIn(ExperimentalDecomposeApi::class)

package com.plusmobileapps.chefmate.settings.root

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderScreen
import com.plusmobileapps.chefmate.settings.AppSettingsScreen
import com.plusmobileapps.chefmate.ui.components.PlusResponsiveContainer
import com.plusmobileapps.chefmate.ui.components.WindowSizeClass

@Composable
fun SettingsRootScreen(bloc: SettingsRootBloc, modifier: Modifier = Modifier) {
    val panels by bloc.panels.subscribeAsState()
    val main = panels.main
    val details = panels.details

    PlusResponsiveContainer(
        modifier = modifier.fillMaxSize().testTag(SettingsRootTestTags.SCREEN)
    ) { windowSizeClass ->
        when (windowSizeClass) {
            WindowSizeClass.COMPACT -> CompactSettingsLayout(main = main, details = details)
            WindowSizeClass.MEDIUM,
            WindowSizeClass.EXPANDED -> DualSettingsLayout(main = main, details = details)
        }
    }
}

@Composable
private fun CompactSettingsLayout(
    main: Child.Created<*, SettingsRootBloc.MainChild>,
    details: Child.Created<*, SettingsRootBloc.DetailsChild>?,
) {
    if (details != null) {
        DetailsContent(details, Modifier.fillMaxSize().testTag(SettingsRootTestTags.DETAILS_PANE))
    } else {
        MasterContent(main, Modifier.fillMaxSize().testTag(SettingsRootTestTags.MASTER_PANE))
    }
}

@Composable
private fun DualSettingsLayout(
    main: Child.Created<*, SettingsRootBloc.MainChild>,
    details: Child.Created<*, SettingsRootBloc.DetailsChild>?,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        MasterContent(
            child = main,
            modifier =
                Modifier.weight(0.4f).fillMaxHeight().testTag(SettingsRootTestTags.MASTER_PANE),
        )
        if (details != null) {
            DetailsContent(
                child = details,
                modifier =
                    Modifier.weight(0.6f).fillMaxHeight().testTag(SettingsRootTestTags.DETAILS_PANE),
            )
        }
    }
}

@Composable
private fun MasterContent(
    child: Child.Created<*, SettingsRootBloc.MainChild>,
    modifier: Modifier = Modifier,
) {
    when (val instance = child.instance) {
        is SettingsRootBloc.MainChild.AppSettings -> AppSettingsScreen(instance.bloc, modifier)
    }
}

@Composable
private fun DetailsContent(
    child: Child.Created<*, SettingsRootBloc.DetailsChild>,
    modifier: Modifier = Modifier,
) {
    when (val instance = child.instance) {
        is SettingsRootBloc.DetailsChild.BottomNavOrder ->
            BottomNavOrderScreen(instance.bloc, modifier)
    }
}
