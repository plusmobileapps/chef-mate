@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.meal.data.impl

import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.testing.createTestDatabase
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.meal.data.impl.remote.MealPlanRemoteDataSource
import com.plusmobileapps.chefmate.meal.data.impl.remote.RemoteMealPlan
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.plusmobileapps.chefmate.util.testing.FakeDateTimeUtil
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class MealPlanRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val db: Database = createTestDatabase()
    private val fakeAuth = FakeAuthenticationRepository()
    private val remote = StubMealPlanRemote()

    private fun repository() =
        MealPlanRepositoryImpl(
            queries = db.mealPlanQueries,
            recipeQueries = db.recipeQueries,
            ioContext = testDispatcher,
            dateTimeUtil = FakeDateTimeUtil(),
            remoteDataSource = remote,
            authRepository = fakeAuth,
            recipeRepository = FakeRecipeRepository(),
        )

    @Test
    fun syncWithRemote_prunes_a_meal_deleted_on_another_device() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val recipeId = insertRecipe(remoteId = "remote-recipe")
            val repo = repository()
            repo.addMeal(recipeId, date = "2026-09-07", mealType = MealType.DINNER)
            repo.addMeal(recipeId, date = "2026-09-08", mealType = MealType.DINNER)
            val meals = db.mealPlanQueries.getSyncedRemoteIds().executeAsList()
            meals.size shouldBe 2

            // The remote only lists the first meal now — the second was deleted elsewhere.
            remote.fetchResult =
                listOf(
                    RemoteMealPlan(
                        id = meals.first().remoteId,
                        ownerId = "test-id",
                        recipeId = "remote-recipe",
                        date = "2026-09-07",
                        mealType = MealType.DINNER.name,
                    )
                )

            repo.syncAllUnsynced()

            db.mealPlanQueries.getById(meals.first().id).executeAsOneOrNull() shouldNotBe null
            db.mealPlanQueries.getById(meals.last().id).executeAsOneOrNull() shouldBe null
        }

    @Test
    fun syncWithRemote_prune_preserves_a_meal_that_has_not_been_pushed_yet() =
        runTest(testDispatcher) {
            val recipeId = insertRecipe(remoteId = "remote-recipe")
            val repo = repository()
            // Added while signed out, so it never got a remote id.
            repo.addMeal(recipeId, date = "2026-09-07", mealType = MealType.DINNER)
            val localId = db.mealPlanQueries.lastInsertId().executeAsOne().MAX!!
            db.mealPlanQueries.getById(localId).executeAsOne().remoteId shouldBe null

            // The push fails on sign-in, so the meal is still unsynced when the prune runs.
            remote.upsertFailure = { RuntimeException("network") }
            fakeAuth.setAuthenticated()

            db.mealPlanQueries.getById(localId).executeAsOneOrNull() shouldNotBe null
        }

    private fun insertRecipe(remoteId: String): Long {
        db.recipeQueries.create(
            title = "Chili",
            description = null,
            ingredients = null,
            directions = null,
            imageUrl = null,
            sourceUrl = null,
            servings = null,
            prepTime = null,
            cookTime = null,
            totalTime = null,
            calories = null,
            starRating = null,
            isFavorite = false,
            createdAt = "now",
            updatedAt = "now",
            clientId = "recipe-client",
            ownerId = null,
        )
        val id = db.recipeQueries.lastInsertId().executeAsOne().MAX!!
        db.recipeQueries.updateRemoteId(remoteId = remoteId, id = id)
        return id
    }

    /** Stamps a stable remote id per meal so tests can correlate, and can fail the push. */
    private class StubMealPlanRemote : MealPlanRemoteDataSource {
        var fetchResult: List<RemoteMealPlan> = emptyList()
        var upsertFailure: (() -> Throwable)? = null
        private var counter = 0

        override suspend fun upsertMealPlan(mealPlan: RemoteMealPlan): RemoteMealPlan {
            upsertFailure?.invoke()?.let { throw it }
            return mealPlan.copy(id = mealPlan.id ?: "remote-meal-${++counter}")
        }

        override suspend fun deleteMealPlan(remoteId: String) = Unit

        override suspend fun fetchAllMealPlans(ownerId: String): List<RemoteMealPlan> = fetchResult
    }
}
