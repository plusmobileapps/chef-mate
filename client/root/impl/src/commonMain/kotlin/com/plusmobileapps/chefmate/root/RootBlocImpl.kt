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
import com.plusmobileapps.chefmate.aichat.AiChatRootBloc
import com.plusmobileapps.chefmate.auth.data.OtpFlow
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.auth.ui.otp.OtpBloc
import com.plusmobileapps.chefmate.browser.BrowserRootBloc
import com.plusmobileapps.chefmate.cook.CookModeBloc
import com.plusmobileapps.chefmate.devsettings.DeveloperSettingsBloc
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.featureflag.FeatureFlags
import com.plusmobileapps.chefmate.featureflag.FeatureFlagsBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.core.addmeal.MealPlannerRootBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc.Props.Detail
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookBloc
import com.plusmobileapps.chefmate.root.RootBloc.Child.BottomNavigation
import com.plusmobileapps.chefmate.root.RootBlocImpl.Configuration.RecipeRoot
import com.plusmobileapps.chefmate.settings.root.SettingsRootBloc
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@AssistedInject
@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = RootBloc.Factory::class)
class RootBlocImpl(
    @Assisted context: BlocContext,
    @Assisted deepLink: DeepLink,
    private val bottomNav: BottomNavBloc.Factory,
    private val browserRootBlocFactory: BrowserRootBloc.Factory,
    private val recipeRoot: RecipeRootBloc.Factory,
    private val mealPlannerRoot: MealPlannerRootBloc.Factory,
    private val authentication: AuthenticationBloc.Factory,
    private val otpBloc: OtpBloc.Factory,
    private val settingsRoot: SettingsRootBloc.Factory,
    private val developerSettings: DeveloperSettingsBloc.Factory,
    private val cookMode: CookModeBloc.Factory,
    private val featureFlags: FeatureFlags,
    private val featureFlagsBlocFactory: FeatureFlagsBloc.Factory,
    private val aiChat: AiChatRootBloc.Factory,
    private val exportRecipes: ExportRecipesBloc.Factory,
    private val editRecipeBook: EditRecipeBookBloc.Factory,
) : RootBloc, BlocContext by context {

    init {
        createScope().launch { featureFlags.refresh() }
    }

    private val initialBottomNavTab: BottomNavBloc.Tab? =
        when (deepLink) {
            DeepLink.Groceries -> BottomNavBloc.Tab.GROCERIES
            DeepLink.MealPlanner -> BottomNavBloc.Tab.MEALS
            else -> null
        }

    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialStack = { initialStackFor(deepLink) },
            handleBackButton = true,
            key = "RootRouter",
            childFactory = ::createChild,
        )

    private fun initialStackFor(deepLink: DeepLink): List<Configuration> =
        when (deepLink) {
            DeepLink.None,
            DeepLink.Groceries,
            DeepLink.MealPlanner -> listOf(Configuration.BottomNavigation)
            is DeepLink.RecipeDetail ->
                listOf(Configuration.BottomNavigation, RecipeRoot(Detail(deepLink.recipeId)))
            DeepLink.AppSettings ->
                listOf(Configuration.BottomNavigation, Configuration.SettingsRoot)
            DeepLink.SignIn ->
                listOf(
                    Configuration.BottomNavigation,
                    Configuration.Authentication(AuthenticationBloc.Props.SignIn),
                )
            DeepLink.SignUp ->
                listOf(
                    Configuration.BottomNavigation,
                    Configuration.Authentication(AuthenticationBloc.Props.SignUp),
                )
        }

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
                    bottomNav.create(
                        context = context,
                        output = ::handleBottomNavOutput,
                        initialTab = initialBottomNavTab,
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
                            presentation =
                                BrowserRootBloc.Presentation.Modal(onClose = { navigation.pop() }),
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

            Configuration.SettingsRoot ->
                RootBloc.Child.SettingsRoot(
                    bloc =
                        settingsRoot.create(context = context, output = ::handleSettingsRootOutput)
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

            Configuration.AiChat ->
                RootBloc.Child.AiChat(
                    bloc = aiChat.create(context = context, output = ::handleAiChatOutput)
                )

            is Configuration.ExportRecipes ->
                RootBloc.Child.ExportRecipes(
                    bloc =
                        exportRecipes.create(
                            context = context,
                            props =
                                config.recipeIds?.let { ExportRecipesBloc.Props.Selected(it) }
                                    ?: ExportRecipesBloc.Props.All,
                            output = ::handleExportRecipesOutput,
                        )
                )

            is Configuration.EditRecipeBook ->
                RootBloc.Child.EditRecipeBook(
                    bloc =
                        editRecipeBook.create(
                            context = context,
                            props =
                                config.bookId?.let { EditRecipeBookBloc.Props.Edit(it) }
                                    ?: EditRecipeBookBloc.Props.Create,
                            output = ::handleEditRecipeBookOutput,
                        )
                )
        }

    private fun handleBottomNavOutput(output: BottomNavBloc.Output) {
        when (output) {
            BottomNavBloc.Output.AddNewRecipe -> {
                navigation.bringToFront(RecipeRoot(RecipeRootBloc.Props.Create))
            }

            is BottomNavBloc.Output.OpenRecipe -> {
                navigation.bringToFront(RecipeRoot(Detail(output.recipeId)))
            }

            is BottomNavBloc.Output.OpenExtractedRecipe -> {
                navigation.bringToFront(
                    RecipeRoot(
                        RecipeRootBloc.Props.CreateFromExtracted(
                            extracted = output.extracted,
                            consumePendingPhoto = output.consumePendingPhoto,
                        )
                    )
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
                navigation.bringToFront(Configuration.SettingsRoot)
            }

            BottomNavBloc.Output.OpenAiChat -> {
                navigation.bringToFront(Configuration.AiChat)
            }

            BottomNavBloc.Output.OpenDeveloperSettings -> {
                navigation.bringToFront(Configuration.DeveloperSettings)
            }

            is BottomNavBloc.Output.OpenCookMode -> {
                navigation.bringToFront(Configuration.CookMode(output.recipeId))
            }

            is BottomNavBloc.Output.OpenExportRecipes -> {
                navigation.bringToFront(Configuration.ExportRecipes(output.recipeIds))
            }

            is BottomNavBloc.Output.OpenEditRecipeBook -> {
                navigation.bringToFront(Configuration.EditRecipeBook(output.bookId))
            }
        }
    }

    private fun handleExportRecipesOutput(output: ExportRecipesBloc.Output) {
        when (output) {
            ExportRecipesBloc.Output.Back -> navigation.pop()
            ExportRecipesBloc.Output.Finished -> {
                navigation.pop()
                // Tell the recipe list to drop multi-select mode now that the archive was written.
                stack.value.items
                    .map { it.instance }
                    .filterIsInstance<RootBloc.Child.BottomNavigation>()
                    .firstOrNull()
                    ?.bloc
                    ?.onExportFinished()
            }
        }
    }

    private fun handleEditRecipeBookOutput(output: EditRecipeBookBloc.Output) {
        when (output) {
            EditRecipeBookBloc.Output.Finished -> navigation.pop()
        }
    }

    private fun handleSettingsRootOutput(output: SettingsRootBloc.Output) {
        when (output) {
            SettingsRootBloc.Output.Back -> navigation.pop()
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

    private fun handleAiChatOutput(output: AiChatRootBloc.Output) {
        when (output) {
            AiChatRootBloc.Output.Finished -> navigation.pop()
            is AiChatRootBloc.Output.AddAsRecipe ->
                navigation.bringToFront(
                    RecipeRoot(
                        RecipeRootBloc.Props.CreateFromExtracted(
                            extracted = output.extracted,
                            fromAi = true,
                            consumePendingPhoto = output.consumePendingPhoto,
                        )
                    )
                )
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
            is AuthenticationBloc.Output.EmailChangeRequired ->
                navigation.bringToFront(
                    Configuration.OtpVerification(email = output.email, flow = OtpFlow.EmailChange)
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

        @Serializable data class RecipeRoot(val props: RecipeRootBloc.Props) : Configuration()

        @Serializable
        data class Authentication(val props: AuthenticationBloc.Props) : Configuration()

        @Serializable
        data class OtpVerification(val email: String, val flow: OtpFlow) : Configuration()

        @Serializable data class Browser(val url: String) : Configuration()

        @Serializable data class MealPlanner(val props: MealPlannerRootBloc.Props) : Configuration()

        @Serializable data object SettingsRoot : Configuration()

        @Serializable data object DeveloperSettings : Configuration()

        @Serializable data object FeatureFlags : Configuration()

        @Serializable data class CookMode(val recipeId: Long) : Configuration()

        @Serializable data object AiChat : Configuration()

        @Serializable data class ExportRecipes(val recipeIds: Set<Long>?) : Configuration()

        @Serializable data class EditRecipeBook(val bookId: Long?) : Configuration()
    }
}
