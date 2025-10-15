package com.plusmobileapps.chefmate.recipe.bottomnav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.plusmobileapps.chefmate.grocery.list.GroceryListScreen

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
                is BottomNavBloc.Child.RecipeList -> Text("Recipes")
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
                icon = {
                    Icon(
                        when (tab) {
                            BottomNavBloc.Tab.RECIPES -> Icons.AutoMirrored.Filled.List
                            BottomNavBloc.Tab.GROCERIES -> Icons.Default.Shop
                        },
                        contentDescription = null,
                    )
                }
            )
        }
    }
}