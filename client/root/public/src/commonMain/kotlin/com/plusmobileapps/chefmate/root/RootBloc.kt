package com.plusmobileapps.chefmate.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.auth.ui.otp.OtpBloc
import com.plusmobileapps.chefmate.browser.BrowserRootBloc
import com.plusmobileapps.chefmate.cook.CookModeBloc
import com.plusmobileapps.chefmate.devsettings.DeveloperSettingsBloc
import com.plusmobileapps.chefmate.featureflag.FeatureFlagsBloc
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderBloc
import com.plusmobileapps.chefmate.recipe.core.addmeal.MealPlannerRootBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.chefmate.settings.AppSettingsBloc
import com.plusmobileapps.chefmate.ui.BlocScreen

interface RootBloc : BackHandlerOwner, BackClickBloc {
    val state: Value<ChildStack<*, Child>>

    fun handleSharedUrl(url: String)

    sealed class Child {
        data class BottomNavigation(val bloc: BottomNavBloc) : Child()

        data class GroceryDetail(val bloc: GroceryDetailBloc) : Child()

        data class RecipeRoot(val bloc: RecipeRootBloc) : Child()

        data class Authentication(val bloc: AuthenticationBloc) : Child(), BlocScreen by bloc

        data class OtpVerification(val bloc: OtpBloc) : Child(), BlocScreen by bloc

        data class Browser(val bloc: BrowserRootBloc) : Child()

        data class MealPlanner(val bloc: MealPlannerRootBloc) : Child()

        data class AppSettings(val bloc: AppSettingsBloc) : Child(), BlocScreen by bloc

        data class BottomNavOrder(val bloc: BottomNavOrderBloc) : Child(), BlocScreen by bloc

        data class DeveloperSettings(val bloc: DeveloperSettingsBloc) : Child(), BlocScreen by bloc

        data class FeatureFlags(val bloc: FeatureFlagsBloc) : Child(), BlocScreen by bloc

        data class CookMode(val bloc: CookModeBloc) : Child(), BlocScreen by bloc
    }

    fun interface Factory {
        fun create(context: BlocContext): RootBloc
    }
}
