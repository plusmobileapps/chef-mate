@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.core.impl.edit

import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class EditRecipeViewModelTest {

    private val recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val repository = FakeRecipeRepository(recipes)
    private val mainContext = UnconfinedTestDispatcher()

    private fun createViewModel(
        recipeId: Long? = null,
        extractedRecipe: ExtractedRecipeData? = null,
    ) =
        EditRecipeViewModel(
            recipeId = recipeId,
            extractedRecipe = extractedRecipe,
            mainContext = mainContext,
            repository = repository,
        )

    @Test
    fun When_no_recipe_id_or_extracted_data_Then_all_fields_start_empty() {
        val vm = createViewModel()
        vm.title.value shouldBe ""
        vm.description.value shouldBe ""
        vm.ingredients.value shouldBe ""
        vm.directions.value shouldBe ""
        vm.imageUrl.value shouldBe ""
        vm.sourceUrl.value shouldBe ""
        vm.servings.value shouldBe ""
        vm.prepTime.value shouldBe ""
        vm.cookTime.value shouldBe ""
        vm.totalTime.value shouldBe ""
        vm.calories.value shouldBe ""
        vm.starRating.value shouldBe null
    }

    @Test
    fun When_extracted_recipe_provided_Then_all_fields_seeded() {
        val extracted =
            ExtractedRecipeData(
                title = "Extracted Title",
                description = "Extracted description",
                ingredients = listOf("1 cup flour", "2 eggs"),
                directions = listOf("Step 1", "Step 2"),
                imageUrl = "https://example.com/image.jpg",
                sourceUrl = "https://example.com/recipe",
                servings = 4,
                prepTime = 10,
                cookTime = 20,
                totalTime = 30,
                calories = 250,
            )

        val vm = createViewModel(extractedRecipe = extracted)

        vm.title.value shouldBe "Extracted Title"
        vm.description.value shouldBe "Extracted description"
        vm.ingredients.value shouldBe "1 cup flour\n2 eggs"
        vm.directions.value shouldBe "Step 1\nStep 2"
        vm.imageUrl.value shouldBe "https://example.com/image.jpg"
        vm.sourceUrl.value shouldBe "https://example.com/recipe"
        vm.servings.value shouldBe "4"
        vm.prepTime.value shouldBe "10"
        vm.cookTime.value shouldBe "20"
        vm.totalTime.value shouldBe "30"
        vm.calories.value shouldBe "250"
        // starRating is not part of extracted data; remains null.
        vm.starRating.value shouldBe null
    }

    @Test
    fun When_extracted_recipe_has_null_optional_fields_Then_those_fields_are_empty() {
        val extracted =
            ExtractedRecipeData(
                title = "Sparse",
                description = null,
                ingredients = emptyList(),
                directions = emptyList(),
                imageUrl = null,
                sourceUrl = "https://example.com",
                servings = null,
                prepTime = null,
                cookTime = null,
                totalTime = null,
                calories = null,
            )

        val vm = createViewModel(extractedRecipe = extracted)

        vm.title.value shouldBe "Sparse"
        vm.description.value shouldBe ""
        vm.ingredients.value shouldBe ""
        vm.directions.value shouldBe ""
        vm.imageUrl.value shouldBe ""
        vm.servings.value shouldBe ""
        vm.prepTime.value shouldBe ""
        vm.cookTime.value shouldBe ""
        vm.totalTime.value shouldBe ""
        vm.calories.value shouldBe ""
    }

    @Test
    fun When_recipe_id_provided_Then_extracted_data_is_ignored() = runTest {
        val existing =
            Recipe(
                id = 7,
                title = "Existing",
                description = "Existing desc",
                ingredients = "existing ingredients",
                directions = "existing directions",
                imageUrl = null,
                sourceUrl = null,
                servings = null,
                prepTime = null,
                cookTime = null,
                totalTime = null,
                calories = null,
                starRating = null,
                isFavorite = false,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        recipes.value = listOf(existing)

        val extracted =
            ExtractedRecipeData(
                title = "Extracted",
                description = null,
                ingredients = listOf("ignored"),
                directions = listOf("ignored"),
                imageUrl = null,
                sourceUrl = "https://example.com",
                servings = null,
                prepTime = null,
                cookTime = null,
                totalTime = null,
                calories = null,
            )

        val vm = createViewModel(recipeId = 7, extractedRecipe = extracted)

        // Existing recipe wins over extracted seed.
        vm.title.value shouldBe "Existing"
        vm.ingredients.value shouldBe "existing ingredients"
    }

    @Test
    fun When_save_with_extracted_seed_Then_repository_creates_new_recipe() = runTest {
        val extracted =
            ExtractedRecipeData(
                title = "From Web",
                description = null,
                ingredients = listOf("flour"),
                directions = listOf("mix"),
                imageUrl = null,
                sourceUrl = "https://example.com/recipe",
                servings = 4,
                prepTime = null,
                cookTime = null,
                totalTime = null,
                calories = null,
            )
        val vm = createViewModel(extractedRecipe = extracted)

        vm.save()
        val saved = vm.output.first()
        saved.shouldBeFinished()
        recipes.value.size shouldBe 1
        recipes.value.first().title shouldBe "From Web"
        recipes.value.first().ingredients shouldBe "flour"
        recipes.value.first().sourceUrl shouldBe "https://example.com/recipe"
    }

    private fun EditRecipeViewModel.Output.shouldBeFinished() {
        check(this is EditRecipeViewModel.Output.Finished)
    }
}
