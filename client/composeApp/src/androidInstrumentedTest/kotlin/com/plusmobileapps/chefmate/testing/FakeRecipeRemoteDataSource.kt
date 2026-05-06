package com.plusmobileapps.chefmate.testing

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.data.impl.remote.RecipeRemoteDataSource
import com.plusmobileapps.chefmate.recipe.data.impl.remote.RemoteRecipe
import com.plusmobileapps.chefmate.recipe.data.impl.remote.SupabaseRecipeRemoteDataSource
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Replaces [SupabaseRecipeRemoteDataSource] in instrumented tests so the test never makes a network
 * call. The real recipe data flow (repository → DB) is exercised; only the remote boundary is
 * faked.
 */
@SingleIn(AppScope::class)
@Inject
@ContributesBinding(scope = AppScope::class, replaces = [SupabaseRecipeRemoteDataSource::class])
class FakeRecipeRemoteDataSource : RecipeRemoteDataSource {
    override suspend fun upsertRecipe(recipe: RemoteRecipe): RemoteRecipe = recipe

    override suspend fun deleteRecipe(remoteId: String) = Unit

    override suspend fun fetchAllRecipes(ownerId: String): List<RemoteRecipe> = emptyList()
}
