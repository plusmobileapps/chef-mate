@file:OptIn(ExperimentalDecomposeApi::class)

package com.plusmobileapps.chefmate.settings.root

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
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
    AnimatedContent(
        targetState = details,
        transitionSpec = {
            val spec = tween<androidx.compose.ui.unit.IntOffset>(durationMillis = 300)
            if (targetState != null) {
                slideInHorizontally(spec) { it } togetherWith slideOutHorizontally(spec) { -it / 4 }
            } else {
                slideInHorizontally(spec) { -it / 4 } togetherWith slideOutHorizontally(spec) { it }
            }
        },
        label = "settings-compact",
    ) { d ->
        if (d != null) {
            DetailsContent(d, Modifier.fillMaxSize().testTag(SettingsRootTestTags.DETAILS_PANE))
        } else {
            MasterContent(main, Modifier.fillMaxSize().testTag(SettingsRootTestTags.MASTER_PANE))
        }
    }
}

/**
 * Wide-window layout. The master pane begins centered with a max width of 480dp. When a detail is
 * opened it slides to the left (capped to 40% of width) and the details pane fades + scales in on
 * the right, mirroring the bottom-nav tab transition.
 */
@Composable
private fun DualSettingsLayout(
    main: Child.Created<*, SettingsRootBloc.MainChild>,
    details: Child.Created<*, SettingsRootBloc.DetailsChild>?,
) {
    val detailsVisible = details != null
    val progress by
        animateFloatAsState(
            targetValue = if (detailsVisible) 1f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "settings-dual-progress",
        )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val fullWidth = maxWidth
        val centeredMasterWidth = minOf(480.dp, fullWidth)
        val dualMasterWidth = minOf(fullWidth * 0.4f, 480.dp).coerceAtLeast(280.dp)
        val masterWidth = lerp(centeredMasterWidth, dualMasterWidth, progress)
        val centeredLeft = (fullWidth - centeredMasterWidth) / 2
        val masterLeft = lerp(centeredLeft, 0.dp, progress)
        val detailsLeft = dualMasterWidth
        val detailsWidth = (fullWidth - dualMasterWidth).coerceAtLeast(0.dp)

        MasterContent(
            child = main,
            modifier =
                Modifier.offset(x = masterLeft)
                    .width(masterWidth)
                    .fillMaxHeight()
                    .testTag(SettingsRootTestTags.MASTER_PANE),
        )

        AnimatedContent(
            modifier =
                Modifier.offset(x = detailsLeft)
                    .width(detailsWidth)
                    .fillMaxHeight()
                    .testTag(SettingsRootTestTags.DETAILS_PANE),
            targetState = details,
            transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
            label = "settings-details",
        ) { d ->
            if (d != null) {
                DetailsContent(d, Modifier.fillMaxSize())
            } else {
                Spacer(Modifier.fillMaxSize())
            }
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
