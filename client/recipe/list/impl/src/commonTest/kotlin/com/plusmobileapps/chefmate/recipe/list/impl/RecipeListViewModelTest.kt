@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.list.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.cook.data.CookingSessionRepository
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeExtractionException
import com.plusmobileapps.chefmate.recipe.data.SyncStatus
import com.plusmobileapps.chefmate.recipe.data.testing.FakeCategoryRepository
import com.plusmobileapps.chefmate.recipe.data.testing.FakePendingRecipePhotoStore
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeImageExtractor
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.plusmobileapps.chefmate.recipe.list.RecipeFilterOption
import com.plusmobileapps.chefmate.recipe.list.RecipeSortOption
import com.russhwolf.settings.Settings
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
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
    private val cookingSessionRepository: CookingSessionRepository = mock {
        every { observeRecipeIds() } returns MutableStateFlow(emptyList())
    }
    private val settings: Settings = mock {
        every { getBoolean("recipe_list_is_grid_view", false) } returns false
        every { getString("recipe_list_sort_option", "RECENTLY_ADDED") } returns "RECENTLY_ADDED"
        every { getString("recipe_list_active_filters", "") } returns ""
        every { getString("recipe_list_active_categories", "") } returns ""
        every { getString("recipe_list_active_user_categories", "") } returns ""
        every { putBoolean(any(), any()) } returns Unit
        every { putString(any(), any()) } returns Unit
    }
    private val categoryRepository = FakeCategoryRepository()
    private val imageExtractor = FakeRecipeImageExtractor()
    private val pendingPhotoStore = FakePendingRecipePhotoStore()
    private val viewModel =
        RecipeListViewModel(
            mainContext = UnconfinedTestDispatcher(),
            repository = repository,
            categoryRepository = categoryRepository,
            cookingSessionRepository = cookingSessionRepository,
            imageExtractor = imageExtractor,
            pendingPhotoStore = pendingPhotoStore,
            settings = settings,
        )

    @Test
    fun When_scan_succeeds_Then_emits_recipe_stores_photo_and_clears_scanning() =
        runTest(UnconfinedTestDispatcher()) {
            imageExtractor.response = sampleExtracted()
            val bytes = byteArrayOf(1, 2, 3)

            viewModel.scannedRecipe.test {
                viewModel.scanRecipeFromPhoto(bytes, "jpg")
                awaitItem() shouldBe sampleExtracted()
                cancelAndIgnoreRemainingEvents()
            }

            pendingPhotoStore.consume()?.fileExtension shouldBe "jpg"
            imageExtractor.calls.single().mimeType shouldBe "image/jpeg"
            viewModel.state.value.isScanning shouldBe false
            viewModel.state.value.scanError shouldBe null
        }

    @Test
    fun When_scan_fails_with_missing_api_key_Then_scan_error_set() {
        imageExtractor.error = RecipeExtractionException("MISSING_API_KEY")

        viewModel.scanRecipeFromPhoto(byteArrayOf(1), "jpg")

        (viewModel.state.value.scanError != null) shouldBe true
        viewModel.state.value.isScanning shouldBe false
    }

    @Test
    fun When_scan_fails_generically_Then_scan_error_set_and_dismissable() {
        imageExtractor.error = RecipeExtractionException("MALFORMED_JSON")

        viewModel.scanRecipeFromPhoto(byteArrayOf(1), "jpg")
        (viewModel.state.value.scanError != null) shouldBe true

        viewModel.dismissScanError()
        viewModel.state.value.scanError shouldBe null
    }

    private fun sampleExtracted() =
        ExtractedRecipeData(
            title = "Scanned",
            description = null,
            ingredients = listOf("flour"),
            directions = listOf("mix"),
            imageUrl = null,
            sourceUrl = "",
            servings = null,
            prepTime = null,
            cookTime = null,
            totalTime = null,
            calories = null,
        )

    private fun recipe(
        id: Long,
        title: String = "Recipe $id",
        isFavorite: Boolean = false,
        starRating: Int? = null,
        totalTime: Int? = null,
        category: BuiltinCategory? = null,
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
            categories =
                category?.let {
                    setOf(Category(id = it.ordinal + 1L, name = it.id, builtinId = it.id))
                } ?: emptySet(),
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
        viewModel.state.value.isGridView shouldBe false
        viewModel.toggleViewMode()
        viewModel.state.value.isGridView shouldBe true
        verify { settings.putBoolean("recipe_list_is_grid_view", true) }
    }

    @Test
    fun When_settings_has_grid_view_true_Then_initial_state_is_grid() {
        val gridSettings: Settings = mock {
            every { getBoolean("recipe_list_is_grid_view", false) } returns true
            every { getString("recipe_list_sort_option", "RECENTLY_ADDED") } returns
                "RECENTLY_ADDED"
            every { getString("recipe_list_active_filters", "") } returns ""
            every { getString("recipe_list_active_categories", "") } returns ""
            every { getString("recipe_list_active_user_categories", "") } returns ""
        }
        val vm =
            RecipeListViewModel(
                mainContext = UnconfinedTestDispatcher(),
                repository = FakeRecipeRepository(),
                categoryRepository = FakeCategoryRepository(),
                cookingSessionRepository = cookingSessionRepository,
                imageExtractor = imageExtractor,
                pendingPhotoStore = pendingPhotoStore,
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

    // region Persistence tests

    @Test
    fun When_sort_updated_Then_persisted_to_settings() {
        viewModel.updateSort(RecipeSortOption.ALPHABETICAL_ASC)
        verify { settings.putString("recipe_list_sort_option", "ALPHABETICAL_ASC") }
    }

    @Test
    fun When_settings_has_sort_option_Then_initial_state_uses_it() {
        val sortSettings: Settings = mock {
            every { getBoolean("recipe_list_is_grid_view", false) } returns false
            every { getString("recipe_list_sort_option", "RECENTLY_ADDED") } returns "TOP_RATED"
            every { getString("recipe_list_active_filters", "") } returns ""
            every { getString("recipe_list_active_categories", "") } returns ""
            every { getString("recipe_list_active_user_categories", "") } returns ""
        }
        val vm =
            RecipeListViewModel(
                mainContext = UnconfinedTestDispatcher(),
                repository = FakeRecipeRepository(),
                categoryRepository = FakeCategoryRepository(),
                cookingSessionRepository = cookingSessionRepository,
                imageExtractor = imageExtractor,
                pendingPhotoStore = pendingPhotoStore,
                settings = sortSettings,
            )
        vm.state.value.currentSort shouldBe RecipeSortOption.TOP_RATED
    }

    @Test
    fun When_filter_toggled_Then_persisted_to_settings() {
        viewModel.toggleFilter(RecipeFilterOption.FAVORITES)
        verify { settings.putString("recipe_list_active_filters", "FAVORITES") }
    }

    @Test
    fun When_settings_has_active_filters_Then_initial_state_uses_them() {
        val filterSettings: Settings = mock {
            every { getBoolean("recipe_list_is_grid_view", false) } returns false
            every { getString("recipe_list_sort_option", "RECENTLY_ADDED") } returns
                "RECENTLY_ADDED"
            every { getString("recipe_list_active_filters", "") } returns "FAVORITES,RATED"
            every { getString("recipe_list_active_categories", "") } returns ""
            every { getString("recipe_list_active_user_categories", "") } returns ""
        }
        val vm =
            RecipeListViewModel(
                mainContext = UnconfinedTestDispatcher(),
                repository = FakeRecipeRepository(),
                categoryRepository = FakeCategoryRepository(),
                cookingSessionRepository = cookingSessionRepository,
                imageExtractor = imageExtractor,
                pendingPhotoStore = pendingPhotoStore,
                settings = filterSettings,
            )
        vm.state.value.activeFilters shouldBe
            setOf(RecipeFilterOption.FAVORITES, RecipeFilterOption.RATED)
    }

    @Test
    fun When_clear_filters_Then_all_filters_removed() {
        recipes.value = listOf(recipe(1, isFavorite = true), recipe(2, isFavorite = false))
        viewModel.toggleFilter(RecipeFilterOption.FAVORITES)
        viewModel.state.value.activeFilters shouldBe setOf(RecipeFilterOption.FAVORITES)
        viewModel.clearFilters()
        viewModel.state.value.activeFilters shouldBe emptySet()
        viewModel.state.value.displayRecipes.size shouldBe 2
    }

    @Test
    fun When_clear_filters_Then_persisted_to_settings() {
        viewModel.toggleFilter(RecipeFilterOption.FAVORITES)
        viewModel.clearFilters()
        verify { settings.putString("recipe_list_active_filters", "") }
    }

    @Test
    fun When_apply_sort_and_filters_Then_both_updated_and_persisted() {
        recipes.value =
            listOf(
                recipe(1, title = "Zucchini", isFavorite = true, starRating = 5),
                recipe(2, title = "Apple", isFavorite = false, starRating = null),
            )
        viewModel.applySortAndFilters(
            RecipeSortOption.ALPHABETICAL_ASC,
            setOf(RecipeFilterOption.FAVORITES, RecipeFilterOption.RATED),
            categories = emptySet(),
            userCategoryIds = emptySet(),
        )
        val state = viewModel.state.value
        state.currentSort shouldBe RecipeSortOption.ALPHABETICAL_ASC
        state.activeFilters shouldBe setOf(RecipeFilterOption.FAVORITES, RecipeFilterOption.RATED)
        state.displayRecipes.map { it.id } shouldBe listOf(1L)
        verify { settings.putString("recipe_list_sort_option", "ALPHABETICAL_ASC") }
        verify { settings.putString("recipe_list_active_filters", any()) }
    }

    // endregion

    // region Category filter tests

    @Test
    fun When_no_category_filter_Then_all_recipes_shown() {
        recipes.value =
            listOf(
                recipe(1, category = BuiltinCategory.BREAKFAST),
                recipe(2, category = BuiltinCategory.DINNER),
                recipe(3, category = null),
            )
        viewModel.state.value.activeCategories shouldBe emptySet()
        viewModel.state.value.displayRecipes.size shouldBe 3
    }

    @Test
    fun When_single_category_filter_Then_only_matching_shown() {
        recipes.value =
            listOf(
                recipe(1, category = BuiltinCategory.BREAKFAST),
                recipe(2, category = BuiltinCategory.DINNER),
                recipe(3, category = null),
            )
        viewModel.applySortAndFilters(
            RecipeSortOption.RECENTLY_ADDED,
            filters = emptySet(),
            categories = setOf(BuiltinCategory.BREAKFAST),
            userCategoryIds = emptySet(),
        )
        viewModel.state.value.displayRecipes.map { it.id } shouldBe listOf(1L)
    }

    @Test
    fun When_multi_category_filter_Then_union_shown() {
        recipes.value =
            listOf(
                recipe(1, category = BuiltinCategory.BREAKFAST),
                recipe(2, category = BuiltinCategory.LUNCH),
                recipe(3, category = BuiltinCategory.DINNER),
            )
        viewModel.applySortAndFilters(
            RecipeSortOption.RECENTLY_ADDED,
            filters = emptySet(),
            categories = setOf(BuiltinCategory.BREAKFAST, BuiltinCategory.LUNCH),
            userCategoryIds = emptySet(),
        )
        viewModel.state.value.displayRecipes.map { it.id }.toSet() shouldBe setOf(1L, 2L)
    }

    @Test
    fun When_filter_excludes_all_Then_empty_result() {
        recipes.value =
            listOf(
                recipe(1, category = BuiltinCategory.BREAKFAST),
                recipe(2, category = BuiltinCategory.LUNCH),
            )
        viewModel.applySortAndFilters(
            RecipeSortOption.RECENTLY_ADDED,
            filters = emptySet(),
            categories = setOf(BuiltinCategory.DESSERT),
            userCategoryIds = emptySet(),
        )
        viewModel.state.value.displayRecipes shouldBe emptyList()
    }

    @Test
    fun When_OTHER_selected_Then_null_category_recipes_match() {
        recipes.value =
            listOf(
                recipe(1, category = BuiltinCategory.BREAKFAST),
                recipe(2, category = null),
                recipe(3, category = BuiltinCategory.OTHER),
            )
        viewModel.applySortAndFilters(
            RecipeSortOption.RECENTLY_ADDED,
            filters = emptySet(),
            categories = setOf(BuiltinCategory.OTHER),
            userCategoryIds = emptySet(),
        )
        viewModel.state.value.displayRecipes.map { it.id }.toSet() shouldBe setOf(2L, 3L)
    }

    @Test
    fun When_OTHER_not_selected_Then_null_category_recipes_excluded() {
        recipes.value =
            listOf(recipe(1, category = BuiltinCategory.BREAKFAST), recipe(2, category = null))
        viewModel.applySortAndFilters(
            RecipeSortOption.RECENTLY_ADDED,
            filters = emptySet(),
            categories = setOf(BuiltinCategory.BREAKFAST),
            userCategoryIds = emptySet(),
        )
        viewModel.state.value.displayRecipes.map { it.id } shouldBe listOf(1L)
    }

    @Test
    fun When_category_and_legacy_filter_combined_Then_both_applied() {
        recipes.value =
            listOf(
                recipe(1, category = BuiltinCategory.DINNER, isFavorite = true),
                recipe(2, category = BuiltinCategory.DINNER, isFavorite = false),
                recipe(3, category = BuiltinCategory.LUNCH, isFavorite = true),
            )
        viewModel.applySortAndFilters(
            RecipeSortOption.RECENTLY_ADDED,
            filters = setOf(RecipeFilterOption.FAVORITES),
            categories = setOf(BuiltinCategory.DINNER),
            userCategoryIds = emptySet(),
        )
        viewModel.state.value.displayRecipes.map { it.id } shouldBe listOf(1L)
    }

    @Test
    fun When_category_filter_applied_Then_persisted_to_settings() {
        viewModel.applySortAndFilters(
            RecipeSortOption.RECENTLY_ADDED,
            filters = emptySet(),
            categories = setOf(BuiltinCategory.DINNER, BuiltinCategory.LUNCH),
            userCategoryIds = emptySet(),
        )
        verify { settings.putString("recipe_list_active_categories", any()) }
    }

    @Test
    fun When_settings_has_active_categories_Then_initial_state_uses_them() {
        val catSettings: Settings = mock {
            every { getBoolean("recipe_list_is_grid_view", false) } returns false
            every { getString("recipe_list_sort_option", "RECENTLY_ADDED") } returns
                "RECENTLY_ADDED"
            every { getString("recipe_list_active_filters", "") } returns ""
            every { getString("recipe_list_active_categories", "") } returns "breakfast,dinner"
            every { getString("recipe_list_active_user_categories", "") } returns ""
        }
        val vm =
            RecipeListViewModel(
                mainContext = UnconfinedTestDispatcher(),
                repository = FakeRecipeRepository(),
                categoryRepository = FakeCategoryRepository(),
                cookingSessionRepository = cookingSessionRepository,
                imageExtractor = imageExtractor,
                pendingPhotoStore = pendingPhotoStore,
                settings = catSettings,
            )
        vm.state.value.activeCategories shouldBe
            setOf(BuiltinCategory.BREAKFAST, BuiltinCategory.DINNER)
    }

    @Test
    fun When_clear_filters_Then_categories_also_cleared() {
        viewModel.applySortAndFilters(
            RecipeSortOption.RECENTLY_ADDED,
            filters = setOf(RecipeFilterOption.FAVORITES),
            categories = setOf(BuiltinCategory.DINNER),
            userCategoryIds = emptySet(),
        )
        viewModel.clearFilters()
        viewModel.state.value.activeFilters shouldBe emptySet()
        viewModel.state.value.activeCategories shouldBe emptySet()
    }

    // endregion
}
