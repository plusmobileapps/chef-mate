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
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipePhotoStorage
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

    private val recipeRemote = RecordingRecipeRemote()

    private val recipeRepository =
        RecipeRepositoryImpl(
            db = db.recipeQueries,
            joinDb = db.recipeCategoryQueries,
            categoryDb = db.categoryQueries,
            ioContext = testDispatcher,
            dateTimeUtil = dateTimeUtil,
            remoteDataSource = recipeRemote,
            authRepository = fakeAuth,
            photoStorage = FakeRecipePhotoStorage(),
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
    fun createRecipe_when_authenticated_Then_pushes_join_rows_to_remote() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val custom = categoryRepository.createUserCategory("Weeknight")
            // Stamp a remote id directly to simulate that the category's own remote push has
            // already completed (otherwise the join row is filtered out as unsynced).
            db.categoryQueries.updateRemoteId(remoteId = "cat-remote-1", id = custom.id)

            recipeRepository.createRecipe(blankRecipe(title = "Tacos", categories = setOf(custom)))

            val (_, categoryRemoteIds) = recipeRemote.attachmentCalls.last()
            categoryRemoteIds shouldBe setOf("cat-remote-1")
        }

    @Test
    fun updateRecipe_when_authenticated_Then_pushes_replaced_join_rows() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val a = categoryRepository.createUserCategory("A")
            val b = categoryRepository.createUserCategory("B")
            db.categoryQueries.updateRemoteId(remoteId = "cat-a", id = a.id)
            db.categoryQueries.updateRemoteId(remoteId = "cat-b", id = b.id)

            val created =
                recipeRepository.createRecipe(blankRecipe(title = "Tacos", categories = setOf(a)))
            recipeRepository.updateRecipe(created.copy(categories = setOf(b)))

            recipeRemote.attachmentCalls.last().second shouldBe setOf("cat-b")
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

    /**
     * Records every join-row push the repo makes so tests can assert that recipe ↔ category
     * attachments are synced to the remote alongside the recipe row itself.
     */
    private class RecordingRecipeRemote : RecipeRemoteDataSource {
        val attachmentCalls: MutableList<Pair<String, Set<String>>> = mutableListOf()

        override suspend fun upsertRecipe(recipe: RemoteRecipe): RemoteRecipe =
            // Stamp a stable remote id derived from the client id so tests can correlate.
            recipe.copy(id = recipe.id ?: "remote-${recipe.clientId.orEmpty()}")

        override suspend fun deleteRecipe(remoteId: String) = Unit

        override suspend fun fetchAllRecipes(ownerId: String): List<RemoteRecipe> = emptyList()

        override suspend fun setRecipeCategories(
            recipeRemoteId: String,
            categoryRemoteIds: Set<String>,
        ) {
            attachmentCalls += recipeRemoteId to categoryRemoteIds
        }

        override suspend fun fetchRecipeCategoryAttachments(
            ownerId: String
        ): Map<String, Set<String>> = emptyMap()
    }

    private class NoopCategoryRemote : CategoryRemoteDataSource {
        override suspend fun upsertCategory(category: RemoteCategory): RemoteCategory = category

        override suspend fun deleteCategory(remoteId: String) = Unit

        override suspend fun fetchAllCategories(ownerId: String): List<RemoteCategory> = emptyList()
    }
}
