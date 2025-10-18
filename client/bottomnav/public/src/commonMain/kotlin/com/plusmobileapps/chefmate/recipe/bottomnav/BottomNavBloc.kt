package com.plusmobileapps.chefmate.recipe.bottomnav

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.list.GroceryListBloc
import kotlinx.coroutines.flow.StateFlow

interface BottomNavBloc {

    val state: StateFlow<Model>

    val content: Value<ChildStack<*, Child>>

    fun onTabSelected(tab: Tab)


    data class Model(
        val selectedTab: Tab = Tab.RECIPES,
        val tabs: List<Tab> = Tab.entries,
    )

    enum class Tab {
        RECIPES,
        GROCERIES
    }

    sealed class Child {
        data object RecipeList : Child()
        data class GroceryList(val bloc: GroceryListBloc) : Child()
    }

    sealed class Output {
        data class OpenRecipe(val recipeId: Long) : Output()
        object AddNewRecipe : Output()
        data class OpenGrocery(val groceryId: Long) : Output()
    }

    interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): BottomNavBloc
    }
}