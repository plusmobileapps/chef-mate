package com.plusmobileapps.chefmate.recipe.data.impl

import app.cash.sqldelight.coroutines.asFlow
import com.plusmobileapps.chefmate.database.RecipeQueries
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.plusmobileapps.chefmate.util.DateTimeUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import kotlin.coroutines.CoroutineContext
import kotlin.time.Instant
import com.plusmobileapps.chefmate.database.Recipe as DbRecipe

@Inject
@ContributesBinding(scope = AppScope::class, boundType = RecipeRepository::class)
class RecipeRepositoryImpl(
    private val db: RecipeQueries,
    @IO private val ioContext: CoroutineContext,
    private val dateTimeUtil: DateTimeUtil,
) : RecipeRepository {
    override fun getRecipes(): Flow<List<Recipe>> =
        db
            .getAll()
            .asFlow()
            .map { it.executeAsList() }
            .map { it.map { item -> item.toRecipe() } }
            .flowOn(ioContext)

    override suspend fun createRecipe(recipe: Recipe): Recipe =
        withContext(ioContext) {
            db.transactionWithResult {
                db.create(
                    title = recipe.title,
                    description = recipe.description,
                    ingredients = recipe.ingredients,
                    directions = recipe.directions,
                    imageUrl = recipe.imageUrl,
                    sourceUrl = recipe.sourceUrl,
                    servings = recipe.servings?.toLong(),
                    prepTime = recipe.prepTime?.toLong(),
                    cookTime = recipe.cookTime?.toLong(),
                    totalTime = recipe.totalTime?.toLong(),
                    calories = recipe.calories?.toLong(),
                    starRating = recipe.starRating?.toLong(),
                    isFavorite = recipe.isFavorite,
                    createdAt = dateTimeUtil.now.toString(),
                    updatedAt = dateTimeUtil.now.toString(),
                )
                val id =
                    db
                        .lastInsertRowId()
                        .executeAsOne()
                db.getById(id).executeAsOne().toRecipe()
            }
        }

    override suspend fun updateRecipe(recipe: Recipe): Recipe =
        withContext(ioContext) {
            db.transactionWithResult {
                db.update(
                    id = recipe.id,
                    title = recipe.title,
                    description = recipe.description,
                    ingredients = recipe.ingredients,
                    directions = recipe.directions,
                    imageUrl = recipe.imageUrl,
                    sourceUrl = recipe.sourceUrl,
                    servings = recipe.servings?.toLong(),
                    prepTime = recipe.prepTime?.toLong(),
                    cookTime = recipe.cookTime?.toLong(),
                    totalTime = recipe.totalTime?.toLong(),
                    calories = recipe.calories?.toLong(),
                    starRating = recipe.starRating?.toLong(),
                    isFavorite = recipe.isFavorite,
                    updatedAt = dateTimeUtil.now.toString(),
                )
                db.getById(recipe.id).executeAsOne().toRecipe()
            }
        }

    override suspend fun getRecipe(id: Long): Flow<Recipe?> =
        db
            .getById(id)
            .asFlow()
            .map { it.executeAsOneOrNull() }
            .map { it?.toRecipe() }
            .flowOn(ioContext)

    override suspend fun deleteRecipe(id: Long) {
        withContext(ioContext) {
            db.delete(id)
        }
    }

    private fun DbRecipe.toRecipe(): Recipe =
        Recipe(
            id = id,
            title = title,
            description = description,
            ingredients = ingredients.orEmpty(),
            directions = directions.orEmpty(),
            imageUrl = imageUrl,
            sourceUrl = sourceUrl,
            servings = servings?.toInt(),
            prepTime = prepTime?.toInt(),
            cookTime = cookTime?.toInt(),
            totalTime = totalTime?.toInt(),
            calories = calories?.toInt(),
            starRating = starRating?.toInt(),
            isFavorite = isFavorite,
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt),
        )
}
