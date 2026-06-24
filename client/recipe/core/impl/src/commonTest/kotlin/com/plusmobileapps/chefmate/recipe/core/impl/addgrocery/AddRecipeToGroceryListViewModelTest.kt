@file:Suppress("FunctionName")
@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.recipe.core.impl.addgrocery

import com.plusmobileapps.chefmate.grocery.data.testing.FakeGroceryRepository
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.russhwolf.settings.MapSettings
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class AddRecipeToGroceryListViewModelTest {

    private val recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val recipeRepository = FakeRecipeRepository(recipes)
    private val groceryRepository = FakeGroceryRepository()

    private fun recipe(ingredients: String): Recipe =
        Recipe(
            id = 1L,
            title = "Soup",
            description = null,
            ingredients = ingredients,
            directions = "cook",
            imageUrl = null,
            sourceUrl = null,
            servings = 2,
            prepTime = 5,
            cookTime = 10,
            totalTime = 15,
            calories = 100,
            starRating = null,
            isFavorite = false,
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-01-02T00:00:00Z"),
        )

    private fun createViewModel(recipe: Recipe): AddRecipeToGroceryListViewModel {
        recipes.value = listOf(recipe)
        return AddRecipeToGroceryListViewModel(
            recipeId = recipe.id,
            mainContext = UnconfinedTestDispatcher(),
            recipeRepository = recipeRepository,
            groceryRepository = groceryRepository,
            settings = MapSettings(),
        )
    }

    @Test
    fun When_recipe_has_duplicate_ingredient_lines_Then_each_item_has_a_unique_id() {
        // Two identical lines previously collided on raw.hashCode(), producing a duplicate
        // LazyColumn key and crashing the screen.
        val viewModel = createViewModel(recipe(ingredients = "Salt\nSalt"))

        val items = viewModel.state.value.groupedIngredients.flatMap { it.items }

        items.size shouldBe 2
        items.map { it.id }.toSet().size shouldBe 2
    }

    @Test
    fun When_one_of_two_duplicate_ingredients_toggled_Then_only_that_item_changes() {
        // Sharing an id between duplicates would have toggled both rows at once.
        val viewModel = createViewModel(recipe(ingredients = "Salt\nSalt"))
        val items = viewModel.state.value.groupedIngredients.flatMap { it.items }

        viewModel.toggleIngredient(items.first().id)

        val updated = viewModel.state.value.groupedIngredients.flatMap { it.items }
        updated.map { it.isSelected } shouldContainExactly listOf(false, true)
    }
}
