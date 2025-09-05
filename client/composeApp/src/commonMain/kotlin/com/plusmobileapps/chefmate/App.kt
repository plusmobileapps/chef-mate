package com.plusmobileapps.chefmate

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.plusmobileapps.chefmate.grocerylist.GroceryListBlocImpl
import com.plusmobileapps.chefmate.grocerylist.GroceryListScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(context: BlocContext) {
    MaterialTheme {
        GroceryListScreen(GroceryListBlocImpl(context))
    }
}