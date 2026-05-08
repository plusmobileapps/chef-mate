package com.plusmobileapps.chefmate.harness

import com.plusmobileapps.chefmate.di.TestApplicationComponent
import com.plusmobileapps.chefmate.fixtures.TestRecipes
import com.plusmobileapps.chefmate.recipe.data.Recipe

/**
 * High-level test scenarios composing the user's auth state with seeded recipes. Apply via
 * [applyUserState] after building a [TestApplicationComponent].
 */
sealed interface TestUserState {

    /** Signed-in user with recipes already in the local DB. Defaults to [TestRecipes.defaults]. */
    data class Authenticated(val recipes: List<Recipe> = TestRecipes.defaults) : TestUserState

    /** Signed-in user with no recipes yet. */
    data object NewUser : TestUserState

    /**
     * Not signed in, but the local DB has recipes (mirrors a user who saved recipes offline before
     * authenticating, or signed out without clearing local data).
     */
    data class UnauthenticatedWithRecipes(val recipes: List<Recipe> = TestRecipes.defaults) :
        TestUserState
}

fun TestApplicationComponent.applyUserState(state: TestUserState) {
    when (state) {
        is TestUserState.Authenticated -> {
            testAuthenticationRepository.setAuthenticated()
            fakeDatabase.addRecipes(state.recipes)
        }
        TestUserState.NewUser -> {
            testAuthenticationRepository.setAuthenticated()
        }
        is TestUserState.UnauthenticatedWithRecipes -> {
            // FakeAuthenticationRepository defaults to AuthState.Unauthenticated; nothing to do.
            fakeDatabase.addRecipes(state.recipes)
        }
    }
}
