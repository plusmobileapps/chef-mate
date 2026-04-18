@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.list.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.SyncStatus
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.plusmobileapps.chefmate.recipe.list.RecipeFilterOption
import com.plusmobileapps.chefmate.recipe.list.RecipeSortOption
import com.russhwolf.settings.Settings
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class RecipeListViewModelTest {

    private val recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val repository = FakeRecipeRepository(recipes)
    private val settings: Settings = mock {
        every { getBoolean("recipe_list_is_grid_view", false) } returns false
    }
    private val viewModel =
        RecipeListViewModel(
            mainContext = UnconfinedTestDispatcher(),
            repository = repository,
            settings = settings,
        )

    private fun recipe(
        id: Long,
        title: String = "Recipe $id",
        isFavorite: Boolean = false,
        starRating: Int? = null,
        totalTime: Int? = null,
        createdAt: Instant = Instant.fromEpochSeconds(id * 1000),
    ) =
        Recipe(
            id = id,
            title = title,
            description = null,
            ingredients = "",
            directions = "",
            imageUrl = null,
            sourceUrl = null,
            servings = null,
            prepTime = null,
            cookTime = null,
            totalTime = totalTime,
            calories = null,
            starRating = starRating,
            isFavorite = isFavorite,
            syncStatus = SyncStatus.NOT_SYNCED,
            createdAt = createdAt,
            updatedAt = createdAt,
        )

    @Test
    fun initial_state_has_empty_recipes_and_default_sort() {
        val state = viewModel.state.value
        state.recipes shouldBe emptyList()
        state.currentSort shouldBe RecipeSortOption.RECENTLY_ADDED
        state.activeFilters shouldBe emptySet()
        state.isGridView shouldBe false
    }

    @Test
    fun When_recipes_emitted_Then_state_updated() = runTest {
        viewModel.state.test {
            awaitItem().recipes shouldBe emptyList()
            val items = listOf(recipe(1), recipe(2))
            recipes.value = items
            val state = awaitItem()
            state.isLoading shouldBe false
            state.recipes shouldBe items
        }
    }

    @Test
    fun When_delete_recipe_Then_repository_removes_it() = runTest {
        recipes.value = listOf(recipe(1), recipe(2))
        viewModel.deleteRecipe(1)
        recipes.value.map { it.id } shouldBe listOf(2L)
    }

    @Test
    fun When_toggle_favorite_Then_recipe_favorite_is_flipped() = runTest {
        recipes.value = listOf(recipe(1, isFavorite = false))
        viewModel.toggleFavorite(1)
        recipes.value.first().isFavorite shouldBe true
    }

    @Test
    fun When_sort_alphabetical_asc_Then_displayRecipes_sorted_by_title() {
        recipes.value = listOf(recipe(1, title = "Zucchini"), recipe(2, title = "Apple"))
        viewModel.updateSort(RecipeSortOption.ALPHABETICAL_ASC)
        viewModel.state.value.displayRecipes.map { it.title } shouldBe listOf("Apple", "Zucchini")
    }

    @Test
    fun When_sort_alphabetical_desc_Then_displayRecipes_sorted_descending() {
        recipes.value = listOf(recipe(1, title = "Apple"), recipe(2, title = "Zucchini"))
        viewModel.updateSort(RecipeSortOption.ALPHABETICAL_DESC)
        viewModel.state.value.displayRecipes.map { it.title } shouldBe listOf("Zucchini", "Apple")
    }

    @Test
    fun When_sort_oldest_first_Then_displayRecipes_sorted_by_createdAt_ascending() {
        val old = recipe(1, createdAt = Instant.fromEpochSeconds(100))
        val newer = recipe(2, createdAt = Instant.fromEpochSeconds(200))
        recipes.value = listOf(newer, old)
        viewModel.updateSort(RecipeSortOption.OLDEST_FIRST)
        viewModel.state.value.displayRecipes.map { it.id } shouldBe listOf(1L, 2L)
    }

    @Test
    fun When_sort_recently_added_Then_displayRecipes_sorted_by_createdAt_descending() {
        val old = recipe(1, createdAt = Instant.fromEpochSeconds(100))
        val newer = recipe(2, createdAt = Instant.fromEpochSeconds(200))
        recipes.value = listOf(old, newer)
        viewModel.updateSort(RecipeSortOption.RECENTLY_ADDED)
        viewModel.state.value.displayRecipes.map { it.id } shouldBe listOf(2L, 1L)
    }

    @Test
    fun When_sort_top_rated_Then_rated_recipes_sorted_descending() {
        val unrated = recipe(1, starRating = null)
        val threeStars = recipe(2, starRating = 3)
        val fiveStars = recipe(3, starRating = 5)
        recipes.value = listOf(unrated, threeStars, fiveStars)
        viewModel.updateSort(RecipeSortOption.TOP_RATED)
        val rated =
            viewModel.state.value.displayRecipes.filter { it.starRating != null }.map { it.id }
        rated shouldBe listOf(3L, 2L)
    }

    @Test
    fun When_filter_favorites_Then_only_favorites_shown() {
        recipes.value = listOf(recipe(1, isFavorite = true), recipe(2, isFavorite = false))
        viewModel.toggleFilter(RecipeFilterOption.FAVORITES)
        viewModel.state.value.displayRecipes.map { it.id } shouldBe listOf(1L)
        viewModel.state.value.activeFilters shouldBe setOf(RecipeFilterOption.FAVORITES)
    }

    @Test
    fun When_filter_rated_Then_only_rated_shown() {
        recipes.value = listOf(recipe(1, starRating = 4), recipe(2, starRating = null))
        viewModel.toggleFilter(RecipeFilterOption.RATED)
        viewModel.state.value.displayRecipes.map { it.id } shouldBe listOf(1L)
    }

    @Test
    fun When_filter_quick_recipes_Then_only_30min_or_less_shown() {
        recipes.value =
            listOf(
                recipe(1, totalTime = 15),
                recipe(2, totalTime = 30),
                recipe(3, totalTime = 60),
                recipe(4, totalTime = null),
            )
        viewModel.toggleFilter(RecipeFilterOption.QUICK_RECIPES)
        val ids = viewModel.state.value.displayRecipes.map { it.id }.toSet()
        ids shouldBe setOf(1L, 2L)
    }

    @Test
    fun When_filter_toggled_twice_Then_filter_removed() {
        recipes.value = listOf(recipe(1, isFavorite = true), recipe(2, isFavorite = false))
        viewModel.toggleFilter(RecipeFilterOption.FAVORITES)
        viewModel.state.value.activeFilters shouldBe setOf(RecipeFilterOption.FAVORITES)
        viewModel.toggleFilter(RecipeFilterOption.FAVORITES)
        viewModel.state.value.activeFilters shouldBe emptySet()
        viewModel.state.value.displayRecipes.size shouldBe 2
    }

    @Test
    fun When_multiple_filters_applied_Then_all_must_match() {
        recipes.value =
            listOf(
                recipe(1, isFavorite = true, starRating = 5),
                recipe(2, isFavorite = true, starRating = null),
                recipe(3, isFavorite = false, starRating = 5),
            )
        viewModel.toggleFilter(RecipeFilterOption.FAVORITES)
        viewModel.toggleFilter(RecipeFilterOption.RATED)
        viewModel.state.value.displayRecipes.map { it.id } shouldBe listOf(1L)
    }

    @Test
    fun When_toggle_view_mode_Then_isGridView_flipped_and_persisted() {
        every { settings.putBoolean("recipe_list_is_grid_view", true) } returns Unit
        viewModel.state.value.isGridView shouldBe false
        viewModel.toggleViewMode()
        viewModel.state.value.isGridView shouldBe true
        verify { settings.putBoolean("recipe_list_is_grid_view", true) }
    }

    @Test
    fun When_settings_has_grid_view_true_Then_initial_state_is_grid() {
        val gridSettings: Settings = mock {
            every { getBoolean("recipe_list_is_grid_view", false) } returns true
        }
        val vm =
            RecipeListViewModel(
                mainContext = UnconfinedTestDispatcher(),
                repository = FakeRecipeRepository(),
                settings = gridSettings,
            )
        vm.state.value.isGridView shouldBe true
    }

    @Test
    fun When_search_query_set_Then_recipes_filtered_by_title() {
        recipes.value =
            listOf(
                recipe(1, title = "Chicken Parmesan"),
                recipe(2, title = "Beef Stew"),
                recipe(3, title = "Chicken Alfredo"),
            )
        viewModel.updateSearchQuery("chicken")
        viewModel.state.value.displayRecipes.map { it.id }.toSet() shouldBe setOf(1L, 3L)
    }

    @Test
    fun When_search_query_cleared_Then_all_recipes_shown() {
        recipes.value = listOf(recipe(1, title = "Chicken"), recipe(2, title = "Beef"))
        viewModel.updateSearchQuery("chicken")
        viewModel.state.value.displayRecipes.size shouldBe 1
        viewModel.updateSearchQuery("")
        viewModel.state.value.displayRecipes.size shouldBe 2
    }

    @Test
    fun When_search_query_matches_no_recipes_Then_empty_list() {
        recipes.value = listOf(recipe(1, title = "Chicken"), recipe(2, title = "Beef"))
        viewModel.updateSearchQuery("pizza")
        viewModel.state.value.displayRecipes shouldBe emptyList()
        viewModel.state.value.isSearchActive shouldBe true
    }

    @Test
    fun When_search_is_case_insensitive_Then_matches_regardless_of_case() {
        recipes.value = listOf(recipe(1, title = "Chicken Parmesan"))
        viewModel.updateSearchQuery("CHICKEN")
        viewModel.state.value.displayRecipes.size shouldBe 1
    }

    @Test
    fun When_search_and_filter_combined_Then_both_applied() {
        recipes.value =
            listOf(
                recipe(1, title = "Chicken Parmesan", isFavorite = true),
                recipe(2, title = "Chicken Alfredo", isFavorite = false),
                recipe(3, title = "Beef Stew", isFavorite = true),
            )
        viewModel.updateSearchQuery("chicken")
        viewModel.toggleFilter(RecipeFilterOption.FAVORITES)
        viewModel.state.value.displayRecipes.map { it.id } shouldBe listOf(1L)
    }

    @Test
    fun When_filter_and_sort_combined_Then_both_applied() {
        recipes.value =
            listOf(
                recipe(1, title = "Zucchini", isFavorite = true),
                recipe(2, title = "Apple", isFavorite = true),
                recipe(3, title = "Banana", isFavorite = false),
            )
        viewModel.toggleFilter(RecipeFilterOption.FAVORITES)
        viewModel.updateSort(RecipeSortOption.ALPHABETICAL_ASC)
        viewModel.state.value.displayRecipes.map { it.title } shouldBe listOf("Apple", "Zucchini")
    }
}
