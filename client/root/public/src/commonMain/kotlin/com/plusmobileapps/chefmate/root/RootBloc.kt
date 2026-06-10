package com.plusmobileapps.chefmate.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.aichat.AiChatRootBloc
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.auth.ui.otp.OtpBloc
import com.plusmobileapps.chefmate.browser.BrowserRootBloc
import com.plusmobileapps.chefmate.cook.CookModeBloc
import com.plusmobileapps.chefmate.devsettings.DeveloperSettingsBloc
import com.plusmobileapps.chefmate.featureflag.FeatureFlagsBloc
import com.plusmobileapps.chefmate.profile.ManageProfileBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.core.addmeal.MealPlannerRootBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookBloc
import com.plusmobileapps.chefmate.settings.root.SettingsRootBloc
import com.plusmobileapps.chefmate.ui.BlocScreen

interface RootBloc : BackHandlerOwner, BackClickBloc {
    val state: Value<ChildStack<*, Child>>

    fun handleSharedUrl(url: String)

    sealed class Child : BlocScreen {
        data class BottomNavigation(val bloc: BottomNavBloc) : Child(), BlocScreen by bloc

        data class RecipeRoot(val bloc: RecipeRootBloc) : Child(), BlocScreen by bloc

        data class Authentication(val bloc: AuthenticationBloc) : Child(), BlocScreen by bloc

        data class OtpVerification(val bloc: OtpBloc) : Child(), BlocScreen by bloc

        data class Browser(val bloc: BrowserRootBloc) : Child(), BlocScreen by bloc

        data class MealPlanner(val bloc: MealPlannerRootBloc) : Child(), BlocScreen by bloc

        data class SettingsRoot(val bloc: SettingsRootBloc) : Child(), BlocScreen by bloc

        data class ManageProfile(val bloc: ManageProfileBloc) : Child(), BlocScreen by bloc

        data class DeveloperSettings(val bloc: DeveloperSettingsBloc) : Child(), BlocScreen by bloc

        data class FeatureFlags(val bloc: FeatureFlagsBloc) : Child(), BlocScreen by bloc

        data class CookMode(val bloc: CookModeBloc) : Child(), BlocScreen by bloc

        data class AiChat(val bloc: AiChatRootBloc) : Child(), BlocScreen by bloc

        data class ExportRecipes(val bloc: ExportRecipesBloc) : Child(), BlocScreen by bloc

        data class EditRecipeBook(val bloc: EditRecipeBookBloc) : Child(), BlocScreen by bloc
    }

    fun interface Factory {
        fun create(context: BlocContext, deepLink: DeepLink): RootBloc
    }
}
