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
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow

interface BottomNavBloc : BackHandlerOwner, BackClickBloc, BlocScreen {
    val state: StateFlow<Model>

    val content: Value<ChildStack<*, Child>>

    fun onTabSelected(tab: Tab)

    fun handleSharedUrl(url: String)

    /**
     * Called by the root navigator after a launched-from-list export completes successfully.
     * Forwards to the recipe-list child so it can drop multi-select mode.
     */
    fun onExportFinished()

    data class Model(
        val selectedTab: Tab = Tab.RECIPES,
        val tabs: ImmutableList<Tab> = Tab.entries.toImmutableList(),
    )

    enum class Tab {
        RECIPES,
        GROCERIES,
        MEALS,
        BROWSER,
        SETTINGS,
    }

    sealed class Child : BlocScreen {
        data class RecipeList(val bloc: RecipeListBloc) : Child(), BlocScreen by bloc

        data class GroceryList(val bloc: GroceryListBloc) : Child(), BlocScreen by bloc

        data class Meals(val bloc: MealPlanBloc) : Child(), BlocScreen by bloc

        data class Browser(val bloc: BrowserRootBloc) : Child(), BlocScreen by bloc

        data class Settings(val bloc: SettingsBloc) : Child(), BlocScreen by bloc
    }

    sealed class Output {
        data class OpenRecipe(val recipeId: Long) : Output()

        data class OpenExtractedRecipe(
            val extracted: ExtractedRecipeData,
            /** True when the extraction came from a scanned photo whose bytes seed the image. */
            val consumePendingPhoto: Boolean = false,
        ) : Output()

        object AddNewRecipe : Output()

        data object OpenSignIn : Output()

        data object OpenSignUp : Output()

        data object OpenManageProfile : Output()

        data object OpenAppSettings : Output()

        data object OpenAiChat : Output()

        data object OpenDeveloperSettings : Output()

        /** Replay the onboarding flow, launched from the More tab. */
        data object OpenOnboarding : Output()

        data class OpenUrl(val url: String) : Output()

        data class OpenMealPlanner(val props: MealPlannerRootBloc.Props) : Output()

        data class OpenCookMode(val recipeId: Long) : Output()

        /**
         * Forwarded from the recipe list's overflow menu. [recipeIds] is null when the user picked
         * "Export all recipes" and a non-empty set when they exported a multi-select.
         */
        data class OpenExportRecipes(val recipeIds: Set<Long>?) : Output()

        /**
         * Open the create/edit recipe-book modal. [bookId] is null to create a new book and set to
         * an existing book's id to rename it (the "Collaborate" and per-row edit actions).
         */
        data class OpenEditRecipeBook(val bookId: Long?) : Output()

        data class OpenEditGroceryList(val listId: Long) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>, initialTab: Tab?): BottomNavBloc
    }
}
