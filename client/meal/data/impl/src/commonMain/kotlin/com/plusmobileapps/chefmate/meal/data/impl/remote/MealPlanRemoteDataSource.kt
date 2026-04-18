package com.plusmobileapps.chefmate.meal.data.impl.remote

interface MealPlanRemoteDataSource {
    suspend fun upsertMealPlan(mealPlan: RemoteMealPlan): RemoteMealPlan

    suspend fun deleteMealPlan(remoteId: String)

    suspend fun fetchAllMealPlans(ownerId: String): List<RemoteMealPlan>
}
