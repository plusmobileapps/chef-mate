package com.plusmobileapps.chefmate.meal.data.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.plusmobileapps.chefmate.database.MealPlanQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import com.plusmobileapps.chefmate.meal.data.MealPlanRepository
import com.plusmobileapps.chefmate.meal.data.MealType
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class MealPlanRepositoryImpl(
    private val queries: MealPlanQueries,
    @IO private val ioContext: CoroutineContext,
) : MealPlanRepository {

    override fun getMealsByDate(date: String): Flow<List<MealPlanItem>> =
        queries.getByDate(date).asFlow().mapToList(ioContext).map { rows ->
            rows.map { it.toMealPlanItem() }
        }

    override fun getMealsByDateRange(startDate: String, endDate: String): Flow<List<MealPlanItem>> =
        queries.getByDateRange(startDate, endDate).asFlow().mapToList(ioContext).map { rows ->
            rows.map { it.toMealPlanItem() }
        }

    override suspend fun addMeal(recipeId: Long, date: String, mealType: MealType) {
        withContext(ioContext) { queries.insert(recipeId, date, mealType.name) }
    }

    override suspend fun removeMeal(id: Long) {
        withContext(ioContext) { queries.delete(id) }
    }
}

private fun com.plusmobileapps.chefmate.database.GetByDate.toMealPlanItem(): MealPlanItem =
    MealPlanItem(
        id = id,
        recipeId = recipeId,
        recipeTitle = recipeTitle,
        recipeImageUrl = recipeImageUrl,
        date = date,
        mealType = MealType.valueOf(mealType),
    )

private fun com.plusmobileapps.chefmate.database.GetByDateRange.toMealPlanItem(): MealPlanItem =
    MealPlanItem(
        id = id,
        recipeId = recipeId,
        recipeTitle = recipeTitle,
        recipeImageUrl = recipeImageUrl,
        date = date,
        mealType = MealType.valueOf(mealType),
    )
