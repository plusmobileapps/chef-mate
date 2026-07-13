@file:OptIn(ExperimentalDecomposeApi::class)

package com.plusmobileapps.chefmate.recipe.bottomnav

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chefmate.client.bottomnav.public.generated.resources.Res
import chefmate.client.bottomnav.public.generated.resources.tab_browser
import chefmate.client.bottomnav.public.generated.resources.tab_grocery
import chefmate.client.bottomnav.public.generated.resources.tab_meals
import chefmate.client.bottomnav.public.generated.resources.tab_more
import chefmate.client.bottomnav.public.generated.resources.tab_recipes
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.BROWSER
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.GROCERIES
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.MEALS
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.RECIPES
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.SETTINGS
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.components.NavRailItem
import com.plusmobileapps.chefmate.ui.components.PlusNavRailHeaderContainer
import com.plusmobileapps.chefmate.ui.components.reportBottomNavInset
import com.plusmobileapps.chefmate.ui.fadeScalePredictiveBackAnimatable
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private enum class NavigationLayout {
    BOTTOM,
    SIDE_COMPACT,
    SIDE_EXPANDED,
}

@Composable
fun BottomNavigationScreen(bloc: BottomNavBloc, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val layout =
            when {
                maxWidth < 600.dp -> NavigationLayout.BOTTOM
                maxHeight < 480.dp -> NavigationLayout.SIDE_COMPACT
                else -> NavigationLayout.SIDE_EXPANDED
            }
        when (layout) {
            NavigationLayout.BOTTOM -> MobileBottomNavContent(bloc = bloc)
            NavigationLayout.SIDE_COMPACT ->
                SideNavContent(modifier = Modifier.imePadding(), bloc = bloc, expandedItems = false)
            NavigationLayout.SIDE_EXPANDED ->
                SideNavContent(modifier = Modifier.imePadding(), bloc = bloc, expandedItems = true)
        }
    }
}

@Composable
private fun SideNavContent(
    bloc: BottomNavBloc,
    expandedItems: Boolean,
    modifier: Modifier = Modifier,
) {
    val state = bloc.state.collectAsState()

    val navRailItems =
        remember(state.value) {
            val currentState = state.value
            currentState.tabs.map {
                NavRailItem(
                    label = it.getLabel().asTextData(),
                    selected = it == currentState.selectedTab,
                    icon = {
                        TabIcon(tab = it, notificationCount = currentState.notificationCount)
                    },
                    onClick = { bloc.onTabSelected(it) },
                )
            }
        }

    PlusNavRailHeaderContainer(
        modifier = modifier.fillMaxSize(),
        navRail = navRailItems,
        expandedItems = expandedItems,
        content = { BottomNavContentContainer(modifier = Modifier.padding(it), bloc = bloc) },
    )
}

@Composable
private fun MobileBottomNavContent(bloc: BottomNavBloc, modifier: Modifier = Modifier) {
    val state = bloc.state.collectAsState()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { PlusBottomBar(state = state.value, onClick = bloc::onTabSelected) },
    ) { paddingValues ->
        // The bottom bar stays anchored at the bottom of the screen; only the content gets ime
        // padding so an open keyboard pushes the content (e.g. the grocery input) up and shrinks
        // the scrollable area above the bar instead of hiding the navigation.
        //
        // consumeWindowInsets marks the bottom-bar/navigation-bar inset (already applied via
        // paddingValues) as consumed, so imePadding only adds the *remaining* keyboard height.
        // Without this, the open keyboard would be padded on top of the nav-bar inset that it
        // already covers, leaving a gap the size of the bottom bar between the input and keyboard.
        BottomNavContentContainer(
            modifier =
                Modifier.padding(paddingValues).consumeWindowInsets(paddingValues).imePadding(),
            bloc = bloc,
        )
    }
}

@Composable
private fun BottomNavContentContainer(bloc: BottomNavBloc, modifier: Modifier = Modifier) {
    Children(
        modifier = modifier.fillMaxSize(),
        stack = bloc.content,
        animation =
            predictiveBackAnimation(
                backHandler = bloc.backHandler,
                fallbackAnimation = stackAnimation(fade() + scale()),
                onBack = bloc::onBackClicked,
                selector = { backEvent, _, _ ->
                    fadeScalePredictiveBackAnimatable(initialEvent = backEvent)
                },
            ),
    ) { created ->
        created.instance.bloc.Content()
    }
}

@Composable
private fun PlusBottomBar(state: BottomNavBloc.Model, onClick: (BottomNavBloc.Tab) -> Unit) {
    // Report the bar height so the global toast host floats snackbars above it instead of covering
    // it. See LocalBottomNavInset / ToastScaffold.
    BottomAppBar(modifier = Modifier.fillMaxWidth().reportBottomNavInset()) {
        state.tabs.forEach { tab ->
            NavigationBarItem(
                selected = tab == state.selectedTab,
                onClick = { onClick(tab) },
                label = {
                    Text(
                        stringResource(tab.getLabel()),
                        color = ChefMateTheme.colorScheme.onSurface,
                    )
                },
                icon = { TabIcon(tab = tab, notificationCount = state.notificationCount) },
            )
        }
    }
}

/**
 * A tab's icon, wrapped in a count badge on the More ([SETTINGS]) tab when notifications are
 * pending. Shared by the bottom bar and the side nav rail so both surfaces stay in sync.
 */
@Composable
private fun TabIcon(tab: BottomNavBloc.Tab, notificationCount: Int) {
    if (tab == SETTINGS && notificationCount > 0) {
        BadgedBox(badge = { Badge { Text(notificationCount.toString()) } }) {
            Icon(imageVector = tab.getIcon(), contentDescription = null)
        }
    } else {
        Icon(imageVector = tab.getIcon(), contentDescription = null)
    }
}

internal fun BottomNavBloc.Tab.getLabel(): StringResource =
    when (this) {
        RECIPES -> Res.string.tab_recipes
        GROCERIES -> Res.string.tab_grocery
        MEALS -> Res.string.tab_meals
        BROWSER -> Res.string.tab_browser
        SETTINGS -> Res.string.tab_more
    }

internal fun BottomNavBloc.Tab.getIcon() =
    when (this) {
        RECIPES -> Icons.AutoMirrored.Filled.List
        GROCERIES -> Icons.Default.ShoppingCart
        MEALS -> Icons.Default.CalendarMonth
        BROWSER -> Icons.Default.Language
        SETTINGS -> Icons.Default.Menu
    }
