@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.auth.usecase.impl

import com.plusmobileapps.chefmate.aichat.AiChatLocalDataCleaner
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.grocery.data.testing.FakeGroceryRepository
import com.plusmobileapps.chefmate.meal.data.testing.FakeMealPlanRepository
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.testing.FakeCategoryRepository
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SignOutUseCaseImplTest {

    private val authenticationRepository = FakeAuthenticationRepository()
    private val groceryRepository = FakeGroceryRepository()
    private val mealPlanRepository = FakeMealPlanRepository()
    private val recipeRepository = FakeRecipeRepository()
    private val categories =
        MutableStateFlow(
            listOf(
                Category(id = 1L, name = "Dinner"),
                Category(id = 2L, name = "AI", builtinId = "ai"),
            )
        )
    private val categoryRepository = FakeCategoryRepository(categories)
    private var aiChatCleared = false
    private val aiChatLocalDataCleaner = AiChatLocalDataCleaner { aiChatCleared = true }

    private val useCase =
        SignOutUseCaseImpl(
            authenticationRepository = authenticationRepository,
            groceryRepository = groceryRepository,
            mealPlanRepository = mealPlanRepository,
            recipeRepository = recipeRepository,
            categoryRepository = categoryRepository,
            aiChatLocalDataCleaner = aiChatLocalDataCleaner,
        )

    @Test
    fun When_signing_out_Then_local_categories_are_wiped() = runTest {
        useCase()

        categoryRepository.observeUserCategories().first() shouldBe emptyList()
    }

    @Test
    fun When_signing_out_Then_ai_chat_history_is_wiped() = runTest {
        useCase()

        aiChatCleared shouldBe true
    }
}
