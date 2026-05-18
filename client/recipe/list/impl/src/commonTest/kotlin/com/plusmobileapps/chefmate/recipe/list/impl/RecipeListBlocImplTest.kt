@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.list.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.cook.data.CookingSessionRepository
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.SyncStatus
import com.plusmobileapps.chefmate.recipe.data.testing.FakeCategoryRepository
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.plusmobileapps.chefmate.recipe.list.RecipeFilterOption
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc
import com.plusmobileapps.chefmate.recipe.list.RecipeListItem
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.util.TimeFormatterUtil
import com.russhwolf.settings.Settings
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

class RecipeListBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<RecipeListBloc.Output>()
    private val recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val repository = FakeRecipeRepository(recipes)
    private val cookingSessionRepository: CookingSessionRepository = mock {
        every { observeRecipeIds() } returns MutableStateFlow(emptyList())
    }
    private val timeFormatterUtil =
        object : TimeFormatterUtil {
            override fun formatMinutes(totalMinutes: Int): TextData =
                FixedString("${totalMinutes}m")
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

    private val bloc =
        RecipeListBlocImpl(
            context = context,
            output = output,
            viewModelFactory = {
                RecipeListViewModel(
                    mainContext = context.mainContext,
                    repository = repository,
                    categoryRepository = categoryRepository,
                    cookingSessionRepository = cookingSessionRepository,
                    settings = settings,
                )
            },
            timeFormatterUtil = timeFormatterUtil,
        )

    private fun recipe(
        id: Long,
        title: String = "Recipe $id",
        isFavorite: Boolean = false,
        starRating: Int? = null,
        totalTime: Int? = null,
        syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
    ) =
        Recipe(
            id = id,
            title = title,
            description = "Description $id",
            ingredients = "ingredients",
            directions = "directions",
            imageUrl = "http://img/$id",
            sourceUrl = null,
            servings = 4,
            prepTime = null,
            cookTime = null,
            totalTime = totalTime,
            calories = 200,
            starRating = starRating,
            isFavorite = isFavorite,
            syncStatus = syncStatus,
            createdAt = Instant.fromEpochSeconds(id * 1000),
            updatedAt = Instant.fromEpochSeconds(id * 1000),
        )

    private fun listItem(id: Long = 1, title: String = "Test") =
        RecipeListItem(
            id = id,
            title = title,
            description = null,
            imageUrl = null,
            starRating = null,
            totalTime = null,
            formattedTotalTime = null,
            servings = null,
            calories = null,
            isFavorite = false,
        )

    @Test
    fun When_recipe_clicked_Then_OpenRecipe_output_emitted() {
        bloc.onRecipeClicked(listItem(id = 42))
        output.lastValue shouldBe RecipeListBloc.Output.OpenRecipe(42)
    }

    @Test
    fun When_add_recipe_clicked_Then_AddNewRecipe_output_emitted() {
        bloc.onAddRecipeClicked()
        output.lastValue shouldBe RecipeListBloc.Output.AddNewRecipe
    }

    @Test
    fun When_recipes_loaded_Then_mapped_to_RecipeListItem() = runTest {
        bloc.state.test {
            awaitItem() // initial

            recipes.value =
                listOf(recipe(1, totalTime = 45, starRating = 3, syncStatus = SyncStatus.SYNCED))
            val state = awaitItem()
            state.recipes.size shouldBe 1

            val item = state.recipes.first()
            item.id shouldBe 1L
            item.title shouldBe "Recipe 1"
            item.description shouldBe "Description 1"
            item.imageUrl shouldBe "http://img/1"
            item.starRating shouldBe 3
            item.totalTime shouldBe 45
            item.formattedTotalTime shouldBe FixedString("45m")
            item.servings shouldBe 4
            item.calories shouldBe 200
            item.isFavorite shouldBe false
            item.syncStatus shouldBe SyncStatus.SYNCED
        }
    }

    @Test
    fun When_recipe_has_no_totalTime_Then_formattedTotalTime_is_null() = runTest {
        bloc.state.test {
            awaitItem()
            recipes.value = listOf(recipe(1, totalTime = null))
            val item = awaitItem().recipes.first()
            item.formattedTotalTime shouldBe null
        }
    }

    @Test
    fun When_recipe_has_totalTime_Then_formattedTotalTime_is_not_null() = runTest {
        bloc.state.test {
            awaitItem()
            recipes.value = listOf(recipe(1, totalTime = 45))
            val item = awaitItem().recipes.first()
            item.formattedTotalTime.shouldBeInstanceOf<TextData>()
        }
    }

    @Test
    fun When_delete_recipe_Then_recipe_removed_from_state() = runTest {
        bloc.state.test {
            awaitItem()
            recipes.value = listOf(recipe(1), recipe(2))
            awaitItem().recipes.size shouldBe 2

            bloc.onDeleteRecipe(listItem(id = 1))
            val nextState = awaitItem()
            nextState.recipes.size shouldBe 1
            nextState.recipes.first().id shouldBe 2L
        }
    }

    @Test
    fun When_syncStatus_varies_Then_mapped_correctly() = runTest {
        bloc.state.test {
            awaitItem()
            recipes.value =
                listOf(
                    recipe(1, syncStatus = SyncStatus.NOT_SYNCED),
                    recipe(2, syncStatus = SyncStatus.SYNCING),
                    recipe(3, syncStatus = SyncStatus.SYNCED),
                )
            val items = awaitItem().recipes
            val byId = items.associateBy { it.id }
            byId[1L]!!.syncStatus shouldBe SyncStatus.NOT_SYNCED
            byId[2L]!!.syncStatus shouldBe SyncStatus.SYNCING
            byId[3L]!!.syncStatus shouldBe SyncStatus.SYNCED
        }
    }

    @Test
    fun When_clear_filters_Then_all_filters_removed_from_state() = runTest {
        bloc.state.test {
            awaitItem()
            recipes.value = listOf(recipe(1, isFavorite = true), recipe(2, isFavorite = false))
            awaitItem()

            bloc.onFilterToggled(RecipeFilterOption.FAVORITES)
            awaitItem().activeFilters shouldBe setOf(RecipeFilterOption.FAVORITES)

            bloc.onClearFilters()
            val cleared = awaitItem()
            cleared.activeFilters shouldBe emptySet()
            cleared.recipes.size shouldBe 2
        }
    }

    @Test
    fun When_browse_recipes_clicked_Then_OpenBrowser_output_emitted() {
        bloc.onBrowseRecipesClicked()
        output.lastValue shouldBe RecipeListBloc.Output.OpenBrowser
    }

    @Test
    fun When_recipes_loaded_Then_totalRecipeCount_reflects_unfiltered_count() = runTest {
        bloc.state.test {
            awaitItem()
            recipes.value = listOf(recipe(1, isFavorite = true), recipe(2, isFavorite = false))
            awaitItem().totalRecipeCount shouldBe 2

            bloc.onFilterToggled(RecipeFilterOption.FAVORITES)
            val filtered = awaitItem()
            filtered.recipes.size shouldBe 1
            filtered.totalRecipeCount shouldBe 2
        }
    }
}
