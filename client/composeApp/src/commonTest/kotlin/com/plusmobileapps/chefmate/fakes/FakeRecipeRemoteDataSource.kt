package com.plusmobileapps.chefmate.fakes

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.data.impl.remote.RecipeRemoteDataSource
import com.plusmobileapps.chefmate.recipe.data.impl.remote.RemoteRecipe
import com.plusmobileapps.chefmate.recipe.data.impl.remote.SupabaseRecipeRemoteDataSource
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
@ContributesBinding(scope = AppScope::class, replaces = [SupabaseRecipeRemoteDataSource::class])
class FakeRecipeRemoteDataSource : RecipeRemoteDataSource {
    override suspend fun upsertRecipe(recipe: RemoteRecipe): RemoteRecipe = recipe

    override suspend fun deleteRecipe(remoteId: String) = Unit

    override suspend fun fetchAllRecipes(ownerId: String): List<RemoteRecipe> = emptyList()

    override suspend fun setRecipeCategories(
        recipeRemoteId: String,
        categoryRemoteIds: Set<String>,
    ) = Unit

    override suspend fun fetchRecipeCategoryAttachments(ownerId: String): Map<String, Set<String>> =
        emptyMap()

    override suspend fun setRecipeBooks(recipeRemoteId: String, bookRemoteIds: Set<String>) = Unit

    override suspend fun fetchRecipeBookAttachments(ownerId: String): Map<String, Set<String>> =
        emptyMap()
}
