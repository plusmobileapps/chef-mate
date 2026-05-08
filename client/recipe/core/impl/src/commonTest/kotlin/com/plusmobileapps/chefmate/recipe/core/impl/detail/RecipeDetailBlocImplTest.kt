@file:Suppress("FunctionName")
@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.core.impl.detail

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.KeepScreenOnRepository
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import com.plusmobileapps.chefmate.util.DateTimeUtil
import com.plusmobileapps.chefmate.util.TimeFormatterUtil
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

    private fun createBloc(recipe: Recipe? = sampleRecipe): RecipeDetailBlocImpl {
        if (recipe != null) recipes.value = listOf(recipe)
        val keepScreenOnRepository = KeepScreenOnRepository(MapSettings())
        val viewModelFactory = RecipeDetailViewModel.Factory { id ->
            RecipeDetailViewModel(
                recipeId = id,
                mainContext = context.mainContext,
                repository = repository,
                keepScreenOnRepository = keepScreenOnRepository,
            )
        }
        val dateTimeUtil =
            mock<DateTimeUtil>().also { every { it.formatDateTime(any()) } returns "" }
        val timeFormatterUtil = mock<TimeFormatterUtil>()
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
}
