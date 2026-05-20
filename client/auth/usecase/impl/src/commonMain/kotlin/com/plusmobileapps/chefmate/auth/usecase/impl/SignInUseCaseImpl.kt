package com.plusmobileapps.chefmate.auth.usecase.impl

import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.usecase.SignInUseCase
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.meal.data.MealPlanRepository
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SignInUseCaseImpl(
    private val authenticationRepository: AuthenticationRepository,
    private val groceryRepository: GroceryRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
) : SignInUseCase {

    override suspend fun guestRecipesToDiscard(): Int {
        val authState = authenticationRepository.state.value as? AuthState.Authenticated ?: return 0
        if (!authState.user.isAnonymous) return 0
        return recipeRepository.getRecipes().first().size
    }

    override suspend fun invoke(email: String, password: String): Result<Unit> {
        // Wipe before swapping sessions so the post-sign-in sync starts from a clean local DB
        // rather than a mix of anon-owned rows (with stale remoteIds the new uid can't touch)
        // and the real account's pull. Mirrors the order in SignOutUseCaseImpl.
        mealPlanRepository.clearLocalData()
        recipeRepository.clearLocalData()
        groceryRepository.clearLocalData()
        groceryRepository.ensureDefaultList()
        return authenticationRepository.signInWithEmailAndPassword(email, password)
    }
}
