@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.root

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.browser.BrowserRootBloc
import com.plusmobileapps.chefmate.cook.CookModeBloc
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderBloc
import com.plusmobileapps.chefmate.recipe.core.addmeal.MealPlannerRootBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.settings.AppSettingsBloc
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
    var detailOutput: Consumer<GroceryDetailBloc.Output> = Consumer {}
    var recipeOutput: Consumer<RecipeRootBloc.Output> = Consumer {}
    var recipeProps: RecipeRootBloc.Props? = null
    var groceryDetailId: Long? = null
    var appSettingsOutput: Consumer<AppSettingsBloc.Output> = Consumer {}
    var bottomNavOrderOutput: Consumer<BottomNavOrderBloc.Output> = Consumer {}

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
            groceryDetail = { _, id, output ->
                groceryDetailId = id
                detailOutput = output
                mock()
            },
            mealPlannerRoot = MealPlannerRootBloc.Factory { _, _, _ -> mock() },
            authentication = { context, props, output -> mock() },
            appSettings = { _, output ->
                appSettingsOutput = output
                mock()
            },
            bottomNavOrder = { _, output ->
                bottomNavOrderOutput = output
                mock()
            },
            cookMode = CookModeBloc.Factory { _, _, _ -> mock() },
        )

    fun RootBloc.instance(): RootBloc.Child = state.value.active.instance

    @Test
    fun When_initialized_Then_bottom_nav_is_shown() {
        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavigation>()
        rootBloc.state.value.backStack.size shouldBe 0
    }

    @Test
    fun When_bottom_nav_outputs_open_detail_Then_detail_is_shown() {
        val itemId = 123L
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenGrocery(itemId))
        rootBloc.instance() should instanceOf<RootBloc.Child.GroceryDetail>()
        rootBloc.state.value.backStack.size shouldBe 1
        groceryDetailId shouldBe itemId
    }

    @Test
    fun Given_detail_When_detail_outputs_close_Then_bottom_nav_is_shown() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenGrocery(123L))
        rootBloc.instance() should instanceOf<RootBloc.Child.GroceryDetail>()
        detailOutput.onNext(GroceryDetailBloc.Output.Finished)
        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavigation>()
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
    fun When_bottom_nav_outputs_open_app_settings_Then_app_settings_is_shown() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenAppSettings)
        rootBloc.instance() should instanceOf<RootBloc.Child.AppSettings>()
        rootBloc.state.value.backStack.size shouldBe 1
    }

    @Test
    fun Given_app_settings_When_back_outputted_Then_bottom_nav_is_shown() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenAppSettings)
        rootBloc.instance() should instanceOf<RootBloc.Child.AppSettings>()
        appSettingsOutput.onNext(AppSettingsBloc.Output.Back)
        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavigation>()
    }

    @Test
    fun Given_app_settings_When_open_bottom_nav_order_Then_bottom_nav_order_shown() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenAppSettings)
        appSettingsOutput.onNext(AppSettingsBloc.Output.OpenBottomNavOrder)
        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavOrder>()
        rootBloc.state.value.backStack.size shouldBe 2
    }

    @Test
    fun Given_bottom_nav_order_When_back_outputted_Then_app_settings_is_shown() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenAppSettings)
        appSettingsOutput.onNext(AppSettingsBloc.Output.OpenBottomNavOrder)
        bottomNavOrderOutput.onNext(BottomNavOrderBloc.Output.Back)
        rootBloc.instance() should instanceOf<RootBloc.Child.AppSettings>()
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
