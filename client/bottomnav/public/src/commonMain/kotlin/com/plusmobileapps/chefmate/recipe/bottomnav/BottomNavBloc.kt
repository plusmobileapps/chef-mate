package com.plusmobileapps.chefmate.recipe.bottomnav

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.list.GroceryListBloc
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc
import kotlinx.coroutines.flow.StateFlow

interface BottomNavBloc :
    BackHandlerOwner,
    BackClickBloc {
    val state: StateFlow<Model>

    val content: Value<ChildStack<*, Child>>

    fun onTabSelected(tab: Tab)

    data class Model(
        val selectedTab: Tab = Tab.RECIPES,
        val tabs: List<Tab> = Tab.entries,
    )

    enum class Tab {
        RECIPES,
        GROCERIES,
    }

    sealed class Child {
        data class RecipeList(
            val bloc: RecipeListBloc,
        ) : Child()

        data class GroceryList(
            val bloc: GroceryListBloc,
        ) : Child()
    }

    sealed class Output {
        data class OpenRecipe(
            val recipeId: Long,
        ) : Output()

        object AddNewRecipe : Output()

        data class OpenGrocery(
            val groceryId: Long,
        ) : Output()
    }

    interface Factory {
        fun create(
            context: BlocContext,
            output: Consumer<Output>,
        ): BottomNavBloc
    }
}
