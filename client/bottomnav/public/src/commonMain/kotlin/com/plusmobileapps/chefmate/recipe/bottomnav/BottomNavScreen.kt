package com.plusmobileapps.chefmate.recipe.bottomnav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import chef_mate.client.bottomnav.public.generated.resources.Res
import chef_mate.client.bottomnav.public.generated.resources.tab_grocery
import chef_mate.client.bottomnav.public.generated.resources.tab_recipes
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.plusmobileapps.chefmate.grocery.list.GroceryListScreen
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.GROCERIES
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.RECIPES
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomNavigationScreen(bloc: BottomNavBloc) {
    val state = bloc.state.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            PlusBottomBar(
                state = state.value,
                onClick = bloc::onTabSelected
            )
        }
    ) { paddingValues ->
        Children(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues),
            stack = bloc.content,
        ) { created ->
            when (val instance = created.instance) {
                is BottomNavBloc.Child.GroceryList -> GroceryListScreen(instance.bloc)
                is BottomNavBloc.Child.RecipeList -> {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Color.Blue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Recipes: TODO")
                    }
                }
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
                        }
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
                }
            )
        }
    }
}