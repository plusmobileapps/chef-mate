@file:OptIn(ExperimentalDecomposeApi::class)

package com.plusmobileapps.chefmate.recipe.bottomnav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import chefmate.client.bottomnav.public.generated.resources.Res
import chefmate.client.bottomnav.public.generated.resources.tab_grocery
import chefmate.client.bottomnav.public.generated.resources.tab_recipes
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.plusmobileapps.chefmate.grocery.list.GroceryListScreen
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.GROCERIES
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.RECIPES
import com.plusmobileapps.chefmate.recipe.list.RecipeListScreen
import com.plusmobileapps.chefmate.ui.fadeScalePredictiveBackAnimatable
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomNavigationScreen(bloc: BottomNavBloc) {
    val state = bloc.state.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            PlusBottomBar(
                state = state.value,
                onClick = bloc::onTabSelected,
            )
        },
    ) { paddingValues ->
        Children(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
            }
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
                        when (tab) {
                            RECIPES -> stringResource(Res.string.tab_recipes)
                            GROCERIES -> stringResource(Res.string.tab_grocery)
                        },
                    )
                },
                icon = {
                    Icon(
                        when (tab) {
                            RECIPES -> Icons.AutoMirrored.Filled.List
                            GROCERIES -> Icons.Default.ShoppingCart
                        },
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
