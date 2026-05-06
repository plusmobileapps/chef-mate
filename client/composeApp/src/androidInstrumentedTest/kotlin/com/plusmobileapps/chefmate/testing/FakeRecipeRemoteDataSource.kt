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
 *
 * Each interface method's response is configurable via the corresponding `stub*` method. Defaults
 * are no-ops (echo back, empty list).
 */
@SingleIn(AppScope::class)
@Inject
@ContributesBinding(scope = AppScope::class, replaces = [SupabaseRecipeRemoteDataSource::class])
class FakeRecipeRemoteDataSource : RecipeRemoteDataSource {
    private var upsertHandler: suspend (RemoteRecipe) -> RemoteRecipe = { it }
    private var deleteHandler: suspend (String) -> Unit = {}
    private var fetchAllHandler: suspend (String) -> List<RemoteRecipe> = { emptyList() }

    fun stubUpsertRecipe(handler: suspend (RemoteRecipe) -> RemoteRecipe) {
        upsertHandler = handler
    }

    fun stubDeleteRecipe(handler: suspend (String) -> Unit) {
        deleteHandler = handler
    }

    fun stubFetchAllRecipes(recipes: List<RemoteRecipe>) {
        fetchAllHandler = { recipes }
    }

    fun stubFetchAllRecipes(handler: suspend (ownerId: String) -> List<RemoteRecipe>) {
        fetchAllHandler = handler
    }

    override suspend fun upsertRecipe(recipe: RemoteRecipe): RemoteRecipe = upsertHandler(recipe)

    override suspend fun deleteRecipe(remoteId: String) = deleteHandler(remoteId)

    override suspend fun fetchAllRecipes(ownerId: String): List<RemoteRecipe> =
        fetchAllHandler(ownerId)
}
