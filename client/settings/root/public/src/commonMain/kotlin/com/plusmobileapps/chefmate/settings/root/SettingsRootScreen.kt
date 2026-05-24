@file:OptIn(ExperimentalDecomposeApi::class)

package com.plusmobileapps.chefmate.settings.root

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
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
        // COMPACT only ever shows one pane, so the detail keeps its back arrow so the user can
        // surface the master list.
        DetailsContent(
            child = details,
            showBackButton = true,
            // COMPACT panes are at most 600dp wide so the default content cap matches the pane.
            maxContentWidth = Dp.Unspecified,
            modifier = Modifier.fillMaxSize().testTag(SettingsRootTestTags.DETAILS_PANE),
        )
    } else {
        MasterContent(
            child = main,
            maxContentWidth = Dp.Unspecified,
            modifier = Modifier.fillMaxSize().testTag(SettingsRootTestTags.MASTER_PANE),
        )
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
            // Each pane is a fixed weighted column. Pass Dp.Unspecified so the underlying
            // PlusHeaderContainer fills the pane instead of centering its content at 600dp,
            // which would leave large white bars on wide windows.
            maxContentWidth = Dp.Unspecified,
            modifier =
                Modifier.weight(0.4f).fillMaxHeight().testTag(SettingsRootTestTags.MASTER_PANE),
        )
        if (details != null) {
            DetailsContent(
                child = details,
                // In dual layout the detail is always on-screen, so a back arrow would be
                // misleading. Drop it (Parent header) and let only the master keep the back
                // affordance for exiting SettingsRoot.
                showBackButton = false,
                maxContentWidth = Dp.Unspecified,
                modifier =
                    Modifier.weight(0.6f).fillMaxHeight().testTag(SettingsRootTestTags.DETAILS_PANE),
            )
        }
    }
}

@Composable
private fun MasterContent(
    child: Child.Created<*, SettingsRootBloc.MainChild>,
    maxContentWidth: Dp,
    modifier: Modifier = Modifier,
) {
    when (val instance = child.instance) {
        is SettingsRootBloc.MainChild.AppSettings ->
            AppSettingsScreen(
                bloc = instance.bloc,
                modifier = modifier,
                maxContentWidth = maxContentWidth,
            )
    }
}

@Composable
private fun DetailsContent(
    child: Child.Created<*, SettingsRootBloc.DetailsChild>,
    showBackButton: Boolean,
    maxContentWidth: Dp,
    modifier: Modifier = Modifier,
) {
    when (val instance = child.instance) {
        is SettingsRootBloc.DetailsChild.BottomNavOrder ->
            BottomNavOrderScreen(
                bloc = instance.bloc,
                modifier = modifier,
                showBackButton = showBackButton,
                maxContentWidth = maxContentWidth,
            )
    }
}
