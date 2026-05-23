@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.root

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.auth.data.OtpFlow
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.auth.ui.otp.OtpBloc
import com.plusmobileapps.chefmate.browser.BrowserRootBloc
import com.plusmobileapps.chefmate.cook.CookModeBloc
import com.plusmobileapps.chefmate.featureflag.testing.FakeFeatureFlags
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.core.addmeal.MealPlannerRootBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.settings.root.SettingsRootBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import kotlin.test.Test

class RootBlocTest {
    val context = TestBlocContext.create()
    var bottomNavOutput: Consumer<BottomNavBloc.Output> = Consumer {}
    var recipeOutput: Consumer<RecipeRootBloc.Output> = Consumer {}
    var recipeProps: RecipeRootBloc.Props? = null
    var settingsRootOutput: Consumer<SettingsRootBloc.Output> = Consumer {}
    var developerSettingsOutput:
        Consumer<com.plusmobileapps.chefmate.devsettings.DeveloperSettingsBloc.Output> =
        Consumer {}
    var authOutput: Consumer<AuthenticationBloc.Output> = Consumer {}
    var otpOutput: Consumer<OtpBloc.Output> = Consumer {}
    var otpProps: OtpBloc.Props? = null

    val rootBloc =
        RootBlocImpl(
            context = context,
            bottomNav = { context, output ->
                bottomNavOutput = output
                mock()
            },
            browserRootBlocFactory =
                object : BrowserRootBloc.Factory {
                    override fun create(
                        context: BlocContext,
                        output: Consumer<BrowserRootBloc.Output>,
                        initialUrl: String?,
                        showControls: Boolean,
                    ): BrowserRootBloc = mock(MockMode.autoUnit)
                },
            recipeRoot = { context, props, output ->
                recipeOutput = output
                recipeProps = props
                mock()
            },
            mealPlannerRoot = MealPlannerRootBloc.Factory { _, _, _ -> mock() },
            authentication = { _, _, output ->
                authOutput = output
                mock()
            },
            otpBloc = { _, props, output ->
                otpProps = props
                otpOutput = output
                mock()
            },
            settingsRoot = { _, output ->
                settingsRootOutput = output
                mock()
            },
            developerSettings = { _, output ->
                developerSettingsOutput = output
                mock()
            },
            cookMode = CookModeBloc.Factory { _, _, _ -> mock() },
            featureFlags = FakeFeatureFlags(),
            featureFlagsBlocFactory = { _, _ -> mock() },
        )

    fun RootBloc.instance(): RootBloc.Child = state.value.active.instance

    @Test
    fun When_initialized_Then_bottom_nav_is_shown() {
        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavigation>()
        rootBloc.state.value.backStack.size shouldBe 0
    }

    @Test
    fun When_bottom_nav_outputs_create_recipe_Then_recipe_root_shown_with_create_props() {
        bottomNavOutput.onNext(BottomNavBloc.Output.AddNewRecipe)
        rootBloc.instance() should instanceOf<RootBloc.Child.RecipeRoot>()
        rootBloc.state.value.backStack.size shouldBe 1
        recipeProps shouldBe RecipeRootBloc.Props.Create

        recipeOutput.onNext(RecipeRootBloc.Output.Finished)
        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavigation>()
    }

    @Test
    fun When_bottom_nav_outputs_open_recipe_Then_recipe_root_shown_with_detail_props() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenRecipe(123L))
        rootBloc.instance() should instanceOf<RootBloc.Child.RecipeRoot>()
        rootBloc.state.value.backStack.size shouldBe 1
        recipeProps shouldBe RecipeRootBloc.Props.Detail(123L)

        recipeOutput.onNext(RecipeRootBloc.Output.Finished)
        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavigation>()
    }

    @Test
    fun When_bottom_nav_outputs_open_url_Then_browser_is_shown() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenUrl("https://example.com"))
        rootBloc.instance() should instanceOf<RootBloc.Child.Browser>()
        rootBloc.state.value.backStack.size shouldBe 1
    }

    @Test
    fun When_recipe_root_outputs_open_url_Then_browser_is_shown() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenRecipe(123L))
        rootBloc.instance() should instanceOf<RootBloc.Child.RecipeRoot>()

        recipeOutput.onNext(RecipeRootBloc.Output.OpenUrl("https://example.com/recipe"))
        rootBloc.instance() should instanceOf<RootBloc.Child.Browser>()
        rootBloc.state.value.backStack.size shouldBe 2
    }

    @Test
    fun When_bottom_nav_outputs_open_settings_root_Then_settings_root_is_shown() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenSettingsRoot)
        rootBloc.instance() should instanceOf<RootBloc.Child.SettingsRoot>()
        rootBloc.state.value.backStack.size shouldBe 1
    }

    @Test
    fun Given_settings_root_When_back_outputted_Then_bottom_nav_is_shown() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenSettingsRoot)
        rootBloc.instance() should instanceOf<RootBloc.Child.SettingsRoot>()
        settingsRootOutput.onNext(SettingsRootBloc.Output.Back)
        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavigation>()
    }

    @Test
    fun When_dev_settings_outputs_open_feature_flags_Then_feature_flags_shown() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenDeveloperSettings)
        rootBloc.instance() should instanceOf<RootBloc.Child.DeveloperSettings>()
        developerSettingsOutput.onNext(
            com.plusmobileapps.chefmate.devsettings.DeveloperSettingsBloc.Output.OpenFeatureFlags
        )
        rootBloc.instance() should instanceOf<RootBloc.Child.FeatureFlags>()
    }

    @Test
    fun When_auth_outputs_email_verification_Then_otp_screen_shown_with_signup_flow() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenSignUp)
        rootBloc.instance() should instanceOf<RootBloc.Child.Authentication>()

        authOutput.onNext(AuthenticationBloc.Output.EmailVerificationRequired("user@example.com"))

        rootBloc.instance() should instanceOf<RootBloc.Child.OtpVerification>()
        otpProps shouldBe OtpBloc.Props(email = "user@example.com", flow = OtpFlow.SignUp)
    }

    @Test
    fun When_auth_outputs_passwordless_otp_Then_otp_screen_shown_with_passwordless_flow() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenSignIn)
        rootBloc.instance() should instanceOf<RootBloc.Child.Authentication>()

        authOutput.onNext(AuthenticationBloc.Output.PasswordlessOtpSent("user@example.com"))

        rootBloc.instance() should instanceOf<RootBloc.Child.OtpVerification>()
        otpProps shouldBe
            OtpBloc.Props(email = "user@example.com", flow = OtpFlow.PasswordlessSignIn)
    }

    @Test
    fun Given_otp_screen_When_otp_verified_Then_bottom_nav_is_shown() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenSignUp)
        authOutput.onNext(AuthenticationBloc.Output.EmailVerificationRequired("user@example.com"))
        rootBloc.instance() should instanceOf<RootBloc.Child.OtpVerification>()

        otpOutput.onNext(OtpBloc.Output.Verified)

        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavigation>()
    }

    @Test
    fun Given_otp_screen_When_otp_cancelled_Then_returns_to_previous() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenSignUp)
        authOutput.onNext(AuthenticationBloc.Output.EmailVerificationRequired("user@example.com"))
        rootBloc.instance() should instanceOf<RootBloc.Child.OtpVerification>()

        otpOutput.onNext(OtpBloc.Output.Cancelled)

        rootBloc.instance() should instanceOf<RootBloc.Child.Authentication>()
    }

    @Test
    fun When_bottom_nav_outputs_open_extracted_recipe_Then_recipe_root_shown_with_create_from_extracted_props() {
        val extracted =
            ExtractedRecipeData(
                title = "Extracted",
                description = null,
                ingredients = listOf("flour"),
                directions = listOf("mix"),
                imageUrl = null,
                sourceUrl = "https://example.com/recipe",
                servings = 4,
                prepTime = 10,
                cookTime = 20,
                totalTime = 30,
                calories = 200,
            )

        bottomNavOutput.onNext(BottomNavBloc.Output.OpenExtractedRecipe(extracted))

        rootBloc.instance() should instanceOf<RootBloc.Child.RecipeRoot>()
        rootBloc.state.value.backStack.size shouldBe 1
        recipeProps shouldBe RecipeRootBloc.Props.CreateFromExtracted(extracted)

        recipeOutput.onNext(RecipeRootBloc.Output.Finished)
        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavigation>()
    }
}
