@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class, ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.data.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.testing.createTestDatabase
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.SyncStatus
import com.plusmobileapps.chefmate.recipe.data.impl.remote.CategoryRemoteDataSource
import com.plusmobileapps.chefmate.recipe.data.impl.remote.RecipeRemoteDataSource
import com.plusmobileapps.chefmate.recipe.data.impl.remote.RemoteCategory
import com.plusmobileapps.chefmate.recipe.data.impl.remote.RemoteRecipe
import com.plusmobileapps.chefmate.util.testing.FakeDateTimeUtil
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class RecipeRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val db: Database = createTestDatabase()
    private val fakeAuth = FakeAuthenticationRepository()
    private val dateTimeUtil = FakeDateTimeUtil()

    private val recipeRepository =
        RecipeRepositoryImpl(
            db = db.recipeQueries,
            joinDb = db.recipeCategoryQueries,
            ioContext = testDispatcher,
            dateTimeUtil = dateTimeUtil,
            remoteDataSource = NoopRecipeRemote(),
            authRepository = fakeAuth,
        )

    private val categoryRepository =
        CategoryRepositoryImpl(
            db = db.categoryQueries,
            ioContext = testDispatcher,
            remoteDataSource = NoopCategoryRemote(),
            authRepository = fakeAuth,
        )

    @Test
    fun createRecipe_with_categories_attaches_them_via_the_join_table() =
        runTest(testDispatcher) {
            val breakfast = categoryRepository.materializeBuiltin(BuiltinCategory.BREAKFAST)

            recipeRepository.createRecipe(
                blankRecipe(title = "Pancakes", categories = setOf(breakfast))
            )

            recipeRepository.getRecipes().test {
                val recipes = awaitItem()
                recipes.size shouldBe 1
                recipes.first().categories.singleOrNull()?.builtinId shouldBe
                    BuiltinCategory.BREAKFAST.id
            }
        }

    @Test
    fun updateRecipe_diff_syncs_attaches_and_detaches_in_one_call() =
        runTest(testDispatcher) {
            val breakfast = categoryRepository.materializeBuiltin(BuiltinCategory.BREAKFAST)
            val dinner = categoryRepository.materializeBuiltin(BuiltinCategory.DINNER)
            val custom = categoryRepository.createUserCategory("Quick")

            val created =
                recipeRepository.createRecipe(
                    blankRecipe(title = "Tacos", categories = setOf(breakfast, dinner))
                )

            // Replace categories: drop breakfast, add custom. Dinner stays put.
            recipeRepository.updateRecipe(created.copy(categories = setOf(dinner, custom)))

            recipeRepository.getRecipes().test {
                val recipes = awaitItem()
                recipes.first().categories.map { it.id }.toSet() shouldBe
                    setOf(dinner.id, custom.id)
            }
        }

    @Test
    fun getRecipes_filters_by_preset_using_attached_categories() =
        runTest(testDispatcher) {
            val breakfast = categoryRepository.materializeBuiltin(BuiltinCategory.BREAKFAST)
            val dinner = categoryRepository.materializeBuiltin(BuiltinCategory.DINNER)
            recipeRepository.createRecipe(
                blankRecipe(title = "Pancakes", categories = setOf(breakfast))
            )
            recipeRepository.createRecipe(blankRecipe(title = "Steak", categories = setOf(dinner)))

            recipeRepository.getRecipes(presets = setOf(BuiltinCategory.BREAKFAST)).test {
                val filtered = awaitItem()
                filtered.map { it.title } shouldBe listOf("Pancakes")
            }
        }

    @Test
    fun getRecipes_with_OTHER_preset_includes_recipes_with_no_categories() =
        runTest(testDispatcher) {
            val breakfast = categoryRepository.materializeBuiltin(BuiltinCategory.BREAKFAST)
            recipeRepository.createRecipe(
                blankRecipe(title = "Pancakes", categories = setOf(breakfast))
            )
            recipeRepository.createRecipe(blankRecipe(title = "Mystery", categories = emptySet()))

            recipeRepository.getRecipes(presets = setOf(BuiltinCategory.OTHER)).test {
                awaitItem().map { it.title } shouldBe listOf("Mystery")
            }
        }

    private fun blankRecipe(title: String, categories: Set<Category>) =
        Recipe(
            id = -1,
            title = title,
            description = null,
            ingredients = "",
            directions = "",
            imageUrl = null,
            sourceUrl = null,
            servings = null,
            prepTime = null,
            cookTime = null,
            totalTime = null,
            calories = null,
            starRating = null,
            isFavorite = false,
            categories = categories,
            syncStatus = SyncStatus.NOT_SYNCED,
            createdAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST,
        )

    private class NoopRecipeRemote : RecipeRemoteDataSource {
        override suspend fun upsertRecipe(recipe: RemoteRecipe): RemoteRecipe = recipe

        override suspend fun deleteRecipe(remoteId: String) = Unit

        override suspend fun fetchAllRecipes(ownerId: String): List<RemoteRecipe> = emptyList()
    }

    private class NoopCategoryRemote : CategoryRemoteDataSource {
        override suspend fun upsertCategory(category: RemoteCategory): RemoteCategory = category

        override suspend fun deleteCategory(remoteId: String) = Unit

        override suspend fun fetchAllCategories(ownerId: String): List<RemoteCategory> = emptyList()
    }
}
