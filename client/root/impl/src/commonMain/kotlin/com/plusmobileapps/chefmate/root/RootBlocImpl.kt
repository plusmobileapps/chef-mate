@file:OptIn(DelicateDecomposeApi::class)

package com.plusmobileapps.chefmate.root

import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.auth.data.OtpFlow
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.auth.ui.otp.OtpBloc
import com.plusmobileapps.chefmate.browser.BrowserRootBloc
import com.plusmobileapps.chefmate.cook.CookModeBloc
import com.plusmobileapps.chefmate.devsettings.DeveloperSettingsBloc
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.featureflag.FeatureFlags
import com.plusmobileapps.chefmate.featureflag.FeatureFlagsBloc
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderBloc
import com.plusmobileapps.chefmate.recipe.core.addmeal.MealPlannerRootBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc.Props.Detail
import com.plusmobileapps.chefmate.root.RootBloc.Child.BottomNavigation
import com.plusmobileapps.chefmate.root.RootBlocImpl.Configuration.GroceryDetail
import com.plusmobileapps.chefmate.root.RootBlocImpl.Configuration.RecipeRoot
import com.plusmobileapps.chefmate.settings.AppSettingsBloc
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@AssistedInject
@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = RootBloc.Factory::class)
class RootBlocImpl(
    @Assisted context: BlocContext,
    private val bottomNav: BottomNavBloc.Factory,
    private val browserRootBlocFactory: BrowserRootBloc.Factory,
    private val groceryDetail: GroceryDetailBloc.Factory,
    private val recipeRoot: RecipeRootBloc.Factory,
    private val mealPlannerRoot: MealPlannerRootBloc.Factory,
    private val authentication: AuthenticationBloc.Factory,
    private val otpBloc: OtpBloc.Factory,
    private val appSettings: AppSettingsBloc.Factory,
    private val bottomNavOrder: BottomNavOrderBloc.Factory,
    private val developerSettings: DeveloperSettingsBloc.Factory,
    private val cookMode: CookModeBloc.Factory,
    private val featureFlags: FeatureFlags,
    private val featureFlagsBlocFactory: FeatureFlagsBloc.Factory,
) : RootBloc, BlocContext by context {

    init {
        createScope().launch { featureFlags.refresh() }
    }

    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialStack = { listOf(Configuration.BottomNavigation) },
            handleBackButton = true,
            key = "RootRouter",
            childFactory = ::createChild,
        )

    override val state: Value<ChildStack<*, RootBloc.Child>> = stack

    override fun onBackClicked() {
        navigation.pop()
    }

    override fun handleSharedUrl(url: String) {
        val bottomNavChild =
            stack.value.items.map { it.instance }.filterIsInstance<BottomNavigation>().firstOrNull()
        bottomNavChild?.bloc?.handleSharedUrl(url)
        navigation.bringToFront(Configuration.BottomNavigation)
    }

    private fun createChild(config: Configuration, context: BlocContext): RootBloc.Child =
        when (config) {
            Configuration.BottomNavigation ->
                BottomNavigation(
                    bottomNav.create(context = context, output = ::handleBottomNavOutput)
                )

            is Configuration.GroceryDetail ->
                RootBloc.Child.GroceryDetail(
                    bloc =
                        groceryDetail.create(
                            context = context,
                            id = config.itemId,
                            output = ::onDetailOutput,
                        )
                )

            is Configuration.RecipeRoot ->
                RootBloc.Child.RecipeRoot(
                    bloc =
                        recipeRoot.create(
                            context = context,
                            props = config.props,
                            output = ::handleRecipeRootOutput,
                        )
                )

            is Configuration.Authentication ->
                RootBloc.Child.Authentication(
                    bloc =
                        authentication.create(
                            context = context,
                            props = config.props,
                            output = ::handleAuthenticationOutput,
                        )
                )

            is Configuration.OtpVerification ->
                RootBloc.Child.OtpVerification(
                    bloc =
                        otpBloc.create(
                            context = context,
                            props = OtpBloc.Props(email = config.email, flow = config.flow),
                            output = ::handleOtpOutput,
                        )
                )

            is Configuration.Browser ->
                RootBloc.Child.Browser(
                    bloc =
                        browserRootBlocFactory.create(
                            context = context,
                            output = { _ -> navigation.pop() },
                            initialUrl = config.url,
                            showControls = false,
                        )
                )

            is Configuration.MealPlanner ->
                RootBloc.Child.MealPlanner(
                    bloc =
                        mealPlannerRoot.create(
                            context = context,
                            props = config.props,
                            output = ::handleMealPlannerOutput,
                        )
                )

            Configuration.AppSettings ->
                RootBloc.Child.AppSettings(
                    bloc = appSettings.create(context = context, output = ::handleAppSettingsOutput)
                )

            Configuration.BottomNavOrder ->
                RootBloc.Child.BottomNavOrder(
                    bloc =
                        bottomNavOrder.create(
                            context = context,
                            output = ::handleBottomNavOrderOutput,
                        )
                )

            Configuration.DeveloperSettings ->
                RootBloc.Child.DeveloperSettings(
                    bloc =
                        developerSettings.create(
                            context = context,
                            output = ::handleDeveloperSettingsOutput,
                        )
                )

            Configuration.FeatureFlags ->
                RootBloc.Child.FeatureFlags(
                    bloc =
                        featureFlagsBlocFactory.create(
                            context = context,
                            output = ::handleFeatureFlagsOutput,
                        )
                )

            is Configuration.CookMode ->
                RootBloc.Child.CookMode(
                    bloc =
                        cookMode.create(
                            context = context,
                            initialRecipeId = config.recipeId,
                            output = ::handleCookModeOutput,
                        )
                )
        }

    private fun handleBottomNavOutput(output: BottomNavBloc.Output) {
        when (output) {
            BottomNavBloc.Output.AddNewRecipe -> {
                navigation.bringToFront(RecipeRoot(RecipeRootBloc.Props.Create))
            }

            is BottomNavBloc.Output.OpenGrocery -> {
                navigation.bringToFront(GroceryDetail(output.groceryId))
            }

            is BottomNavBloc.Output.OpenRecipe -> {
                navigation.bringToFront(RecipeRoot(Detail(output.recipeId)))
            }

            is BottomNavBloc.Output.OpenExtractedRecipe -> {
                navigation.bringToFront(
                    RecipeRoot(RecipeRootBloc.Props.CreateFromExtracted(output.extracted))
                )
            }

            BottomNavBloc.Output.OpenSignIn -> {
                navigation.bringToFront(
                    Configuration.Authentication(AuthenticationBloc.Props.SignIn)
                )
            }

            BottomNavBloc.Output.OpenSignUp -> {
                navigation.bringToFront(
                    Configuration.Authentication(AuthenticationBloc.Props.SignUp)
                )
            }

            is BottomNavBloc.Output.OpenUrl -> {
                navigation.bringToFront(Configuration.Browser(output.url))
            }

            is BottomNavBloc.Output.OpenMealPlanner -> {
                navigation.bringToFront(Configuration.MealPlanner(output.props))
            }

            BottomNavBloc.Output.OpenAppSettings -> {
                navigation.bringToFront(Configuration.AppSettings)
            }

            BottomNavBloc.Output.OpenDeveloperSettings -> {
                navigation.bringToFront(Configuration.DeveloperSettings)
            }

            is BottomNavBloc.Output.OpenCookMode -> {
                navigation.bringToFront(Configuration.CookMode(output.recipeId))
            }
        }
    }

    private fun handleAppSettingsOutput(output: AppSettingsBloc.Output) {
        when (output) {
            AppSettingsBloc.Output.Back -> navigation.pop()
            AppSettingsBloc.Output.OpenBottomNavOrder -> {
                navigation.bringToFront(Configuration.BottomNavOrder)
            }
        }
    }

    private fun handleBottomNavOrderOutput(output: BottomNavOrderBloc.Output) {
        when (output) {
            BottomNavOrderBloc.Output.Back -> navigation.pop()
        }
    }

    private fun handleDeveloperSettingsOutput(output: DeveloperSettingsBloc.Output) {
        when (output) {
            DeveloperSettingsBloc.Output.Back -> navigation.pop()
            DeveloperSettingsBloc.Output.OpenFeatureFlags -> {
                navigation.bringToFront(Configuration.FeatureFlags)
            }
        }
    }

    private fun handleFeatureFlagsOutput(output: FeatureFlagsBloc.Output) {
        when (output) {
            FeatureFlagsBloc.Output.Back -> navigation.pop()
        }
    }

    private fun onDetailOutput(output: GroceryDetailBloc.Output) {
        when (output) {
            GroceryDetailBloc.Output.Finished -> navigation.pop()
        }
    }

    private fun handleRecipeRootOutput(output: RecipeRootBloc.Output) {
        when (output) {
            RecipeRootBloc.Output.Finished -> navigation.pop()
            is RecipeRootBloc.Output.OpenUrl -> {
                navigation.bringToFront(Configuration.Browser(output.url))
            }
            RecipeRootBloc.Output.OpenGroceryList -> {
                val bottomNavChild =
                    stack.value.items
                        .map { it.instance }
                        .filterIsInstance<BottomNavigation>()
                        .firstOrNull()
                bottomNavChild?.bloc?.onTabSelected(BottomNavBloc.Tab.GROCERIES)
                navigation.bringToFront(Configuration.BottomNavigation)
            }
            is RecipeRootBloc.Output.OpenMealPlanner -> {
                navigation.bringToFront(Configuration.MealPlanner(output.props))
            }
            is RecipeRootBloc.Output.OpenCookMode -> {
                navigation.bringToFront(Configuration.CookMode(output.recipeId))
            }
        }
    }

    private fun handleCookModeOutput(output: CookModeBloc.Output) {
        when (output) {
            CookModeBloc.Output.Finished -> navigation.pop()
        }
    }

    private fun handleMealPlannerOutput(output: MealPlannerRootBloc.Output) {
        when (output) {
            MealPlannerRootBloc.Output.Finished -> navigation.pop()
        }
    }

    private fun handleAuthenticationOutput(output: AuthenticationBloc.Output) {
        when (output) {
            AuthenticationBloc.Output.Finished -> navigation.pop()
            AuthenticationBloc.Output.AuthenticationSuccess -> navigation.pop()
            is AuthenticationBloc.Output.EmailVerificationRequired ->
                navigation.bringToFront(
                    Configuration.OtpVerification(email = output.email, flow = OtpFlow.SignUp)
                )
            is AuthenticationBloc.Output.PasswordlessOtpSent ->
                navigation.bringToFront(
                    Configuration.OtpVerification(
                        email = output.email,
                        flow = OtpFlow.PasswordlessSignIn,
                    )
                )
            is AuthenticationBloc.Output.OpenUrl ->
                navigation.bringToFront(Configuration.Browser(output.url))
        }
    }

    private fun handleOtpOutput(output: OtpBloc.Output) {
        when (output) {
            OtpBloc.Output.Verified -> navigation.bringToFront(Configuration.BottomNavigation)
            OtpBloc.Output.Cancelled -> navigation.pop()
        }
    }

    @Serializable
    private sealed class Configuration {
        @Serializable data object BottomNavigation : Configuration()

        @Serializable data class GroceryDetail(val itemId: Long) : Configuration()

        @Serializable data class RecipeRoot(val props: RecipeRootBloc.Props) : Configuration()

        @Serializable
        data class Authentication(val props: AuthenticationBloc.Props) : Configuration()

        @Serializable
        data class OtpVerification(val email: String, val flow: OtpFlow) : Configuration()

        @Serializable data class Browser(val url: String) : Configuration()

        @Serializable data class MealPlanner(val props: MealPlannerRootBloc.Props) : Configuration()

        @Serializable data object AppSettings : Configuration()

        @Serializable data object BottomNavOrder : Configuration()

        @Serializable data object DeveloperSettings : Configuration()

        @Serializable data object FeatureFlags : Configuration()

        @Serializable data class CookMode(val recipeId: Long) : Configuration()
    }
}
