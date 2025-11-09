@file:OptIn(ExperimentalDecomposeApi::class)

package com.plusmobileapps.chefmate.recipe.bottomnav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import chefmate.client.bottomnav.public.generated.resources.Res
import chefmate.client.bottomnav.public.generated.resources.tab_grocery
import chefmate.client.bottomnav.public.generated.resources.tab_recipes
import chefmate.client.bottomnav.public.generated.resources.tab_settings
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListScreen
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.GROCERIES
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.RECIPES
import com.plusmobileapps.chefmate.recipe.list.RecipeListScreen
import com.plusmobileapps.chefmate.settings.SettingsScreen
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.NavRailItem
import com.plusmobileapps.chefmate.ui.components.PlusNavRailHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusResponsiveContainer
import com.plusmobileapps.chefmate.ui.components.WindowSizeClass
import com.plusmobileapps.chefmate.ui.fadeScalePredictiveBackAnimatable
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomNavigationScreen(bloc: BottomNavBloc) {
    PlusResponsiveContainer { windowSize ->
        when (windowSize) {
            WindowSizeClass.COMPACT ->
                MobileBottomNavContent(
                    modifier = Modifier.imePadding(),
                    bloc = bloc,
                )
            WindowSizeClass.MEDIUM,
            WindowSizeClass.EXPANDED,
            ->
                TabletNavRailContent(
                    modifier = Modifier.imePadding(),
                    bloc = bloc,
                )
        }
    }
}

@Composable
private fun TabletNavRailContent(
    bloc: BottomNavBloc,
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
                    icon = { Icon(imageVector = it.getIcon(), contentDescription = null) },
                    onClick = { bloc.onTabSelected(it) },
                )
            }
        }

    PlusNavRailHeaderContainer(
        modifier = modifier.fillMaxSize(),
        navRail = navRailItems,
        content = {
            BottomNavContentContainer(
                modifier = Modifier.padding(it),
                bloc = bloc,
            )
        },
    )
}

@Composable
private fun MobileBottomNavContent(
    bloc: BottomNavBloc,
    modifier: Modifier = Modifier,
) {
    val state = bloc.state.collectAsState()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            PlusBottomBar(
                state = state.value,
                onClick = bloc::onTabSelected,
            )
        },
    ) { paddingValues ->
        BottomNavContentContainer(
            modifier = Modifier.padding(paddingValues),
            bloc = bloc,
        )
    }
}

@Composable
private fun BottomNavContentContainer(
    bloc: BottomNavBloc,
    modifier: Modifier = Modifier,
) {
    Children(
        modifier = modifier.fillMaxSize(),
        stack = bloc.content,
        animation =
            predictiveBackAnimation(
                backHandler = bloc.backHandler,
                fallbackAnimation = stackAnimation(fade() + scale()),
                onBack = bloc::onBackClicked,
                selector = { backEvent, _, _ ->
                    fadeScalePredictiveBackAnimatable(
                        initialEvent = backEvent,
                    )
                },
            ),
    ) { created ->
        when (val instance = created.instance) {
            is BottomNavBloc.Child.GroceryList -> GroceryListScreen(instance.bloc)
            is BottomNavBloc.Child.RecipeList -> RecipeListScreen(instance.bloc)
            is BottomNavBloc.Child.Settings -> SettingsScreen(instance.bloc)
        }
    }
}

@Composable
private fun PlusBottomBar(
    state: BottomNavBloc.Model,
    onClick: (BottomNavBloc.Tab) -> Unit,
) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
    ) {
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
                icon = {
                    Icon(
                        imageVector = tab.getIcon(),
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

private fun BottomNavBloc.Tab.getLabel(): StringResource =
    when (this) {
        RECIPES -> Res.string.tab_recipes
        GROCERIES -> Res.string.tab_grocery
        BottomNavBloc.Tab.SETTINGS -> Res.string.tab_settings
    }

private fun BottomNavBloc.Tab.getIcon() =
    when (this) {
        RECIPES -> Icons.AutoMirrored.Filled.List
        GROCERIES -> Icons.Default.ShoppingCart
        BottomNavBloc.Tab.SETTINGS -> Icons.Default.Settings
    }
