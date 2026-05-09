package com.plusmobileapps.chefmate.devsettings.impl

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class FakeRecipeSeeder(private val recipeRepository: RecipeRepository) {
    suspend fun seed() {
        Recipe.Samples.forEach { recipeRepository.createRecipe(it) }
    }
}
