package com.plusmobileapps.chefmate.recipe.bottomnav

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.browser.BrowserRootBloc
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc
import com.plusmobileapps.chefmate.meal.core.MealPlanBloc
import com.plusmobileapps.chefmate.recipe.core.addmeal.MealPlannerRootBloc
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc
import com.plusmobileapps.chefmate.settings.SettingsBloc
import kotlinx.coroutines.flow.StateFlow

interface BottomNavBloc : BackHandlerOwner, BackClickBloc {
    val state: StateFlow<Model>

    val content: Value<ChildStack<*, Child>>

    fun onTabSelected(tab: Tab)

    fun handleSharedUrl(url: String)

    data class Model(val selectedTab: Tab = Tab.RECIPES, val tabs: List<Tab> = Tab.entries)

    enum class Tab {
        RECIPES,
        GROCERIES,
        MEALS,
        BROWSER,
        SETTINGS,
    }

    sealed class Child {
        data class RecipeList(val bloc: RecipeListBloc) : Child()

        data class GroceryList(val bloc: GroceryListBloc) : Child()

        data class Meals(val bloc: MealPlanBloc) : Child()

        data class Browser(val bloc: BrowserRootBloc) : Child()

        data class Settings(val bloc: SettingsBloc) : Child()
    }

    sealed class Output {
        data class OpenRecipe(val recipeId: Long) : Output()

        data class OpenExtractedRecipe(val extracted: ExtractedRecipeData) : Output()

        object AddNewRecipe : Output()

        data object OpenSignIn : Output()

        data object OpenSignUp : Output()

        data object OpenSettingsRoot : Output()

        data object OpenDeveloperSettings : Output()

        data class OpenUrl(val url: String) : Output()

        data class OpenMealPlanner(val props: MealPlannerRootBloc.Props) : Output()

        data class OpenCookMode(val recipeId: Long) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): BottomNavBloc
    }
}
