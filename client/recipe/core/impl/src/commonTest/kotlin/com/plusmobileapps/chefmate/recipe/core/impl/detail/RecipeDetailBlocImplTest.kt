@file:Suppress("FunctionName")
@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.core.impl.detail

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.CoachMarkController
import com.plusmobileapps.chefmate.di.CoachMarkId
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.util.TimeFormatterUtil
import com.plusmobileapps.chefmate.util.testing.FakeDateTimeUtil
import com.russhwolf.settings.MapSettings
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow

class RecipeDetailBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<RecipeDetailBloc.Output>()
    private val recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val repository = FakeRecipeRepository(recipes)

    private val sampleRecipe =
        Recipe(
            id = 7L,
            title = "Pancakes",
            description = null,
            ingredients = "flour\negg",
            directions = "mix\ncook",
            imageUrl = "https://example.com/pancakes.jpg",
            sourceUrl = null,
            servings = 2,
            prepTime = 5,
            cookTime = 10,
            totalTime = 15,
            calories = 300,
            starRating = null,
            isFavorite = false,
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-01-02T00:00:00Z"),
        )

    private lateinit var coachMarkController: CoachMarkController

    private fun createBloc(
        recipe: Recipe? = sampleRecipe,
        cookModeTooltipSeen: Boolean = false,
    ): RecipeDetailBlocImpl {
        if (recipe != null) recipes.value = listOf(recipe)
        coachMarkController = CoachMarkController(MapSettings())
        if (cookModeTooltipSeen) coachMarkController.dismiss(CoachMarkId.RECIPE_DETAIL_COOK_MODE)
        val viewModelFactory = RecipeDetailViewModel.Factory { id ->
            RecipeDetailViewModel(
                recipeId = id,
                mainContext = context.mainContext,
                repository = repository,
                coachMarkController = coachMarkController,
            )
        }
        val dateTimeUtil = FakeDateTimeUtil()
        val timeFormatterUtil =
            mock<TimeFormatterUtil>().also {
                every { it.formatMinutes(any()) } returns FixedString("")
            }
        val groceryFactory =
            object : AddRecipeToGroceryListBloc.Factory {
                override fun create(
                    context: BlocContext,
                    recipeId: Long,
                    output: Consumer<AddRecipeToGroceryListBloc.Output>,
                ): AddRecipeToGroceryListBloc = mock()
            }
        return RecipeDetailBlocImpl(
            context = context,
            recipeId = recipe?.id ?: 1L,
            output = output,
            viewModelFactory = viewModelFactory,
            dateTimeUtil = dateTimeUtil,
            timeFormatterUtil = timeFormatterUtil,
            addToGroceryList = groceryFactory,
        )
    }

    @Test
    fun When_onImageClicked_with_imageUrl_Then_full_image_slot_is_active() {
        val bloc = createBloc()
        bloc.onImageClicked()
        val active =
            bloc.fullImageSlot.value.child
                ?.instance
                .shouldBeInstanceOf<RecipeDetailBloc.FullImage.Active>()
        active.imageUrl shouldBe sampleRecipe.imageUrl
        active.recipeId shouldBe sampleRecipe.id
        active.title shouldBe sampleRecipe.title
    }

    @Test
    fun When_onImageClicked_with_null_imageUrl_Then_slot_stays_inactive() {
        val bloc = createBloc(recipe = sampleRecipe.copy(imageUrl = null))
        bloc.onImageClicked()
        bloc.fullImageSlot.value.child shouldBe null
    }

    @Test
    fun When_onCloseFullImage_Then_slot_is_dismissed() {
        val bloc = createBloc()
        bloc.onImageClicked()
        bloc.onCloseFullImage()
        bloc.fullImageSlot.value.child shouldBe null
    }

    @Test
    fun When_onBackClicked_with_active_overlay_Then_overlay_dismisses_and_no_output() {
        val bloc = createBloc()
        bloc.onImageClicked()
        bloc.onBackClicked()
        bloc.fullImageSlot.value.child shouldBe null
        output.values shouldBe emptyList()
    }

    @Test
    fun When_onBackClicked_with_inactive_overlay_Then_output_finished_emitted() {
        val bloc = createBloc()
        bloc.onBackClicked()
        output.lastValue shouldBe RecipeDetailBloc.Output.Finished
    }

    @Test
    fun When_tooltip_not_yet_seen_Then_cook_mode_tooltip_is_shown() {
        val bloc = createBloc(cookModeTooltipSeen = false)
        bloc.state.value.showCookModeTooltip shouldBe true
    }

    @Test
    fun When_tooltip_already_seen_Then_cook_mode_tooltip_is_hidden() {
        val bloc = createBloc(cookModeTooltipSeen = true)
        bloc.state.value.showCookModeTooltip shouldBe false
    }

    @Test
    fun When_onCookModeTooltipDismissed_Then_tooltip_hidden_and_persisted() {
        val bloc = createBloc(cookModeTooltipSeen = false)
        bloc.onCookModeTooltipDismissed()
        bloc.state.value.showCookModeTooltip shouldBe false
        coachMarkController.hasSeen(CoachMarkId.RECIPE_DETAIL_COOK_MODE) shouldBe true
    }

    @Test
    fun When_onCookModeClicked_Then_tooltip_persisted_and_output_emitted() {
        val bloc = createBloc(cookModeTooltipSeen = false)
        bloc.onCookModeClicked()
        bloc.state.value.showCookModeTooltip shouldBe false
        coachMarkController.hasSeen(CoachMarkId.RECIPE_DETAIL_COOK_MODE) shouldBe true
        output.lastValue shouldBe RecipeDetailBloc.Output.OpenCookMode(sampleRecipe.id)
    }
}
