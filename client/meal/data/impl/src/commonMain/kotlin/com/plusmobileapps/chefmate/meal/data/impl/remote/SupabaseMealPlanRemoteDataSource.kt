package com.plusmobileapps.chefmate.meal.data.impl.remote

import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SupabaseMealPlanRemoteDataSource(private val supabaseClient: SupabaseClient) :
    MealPlanRemoteDataSource {

    override suspend fun upsertMealPlan(mealPlan: RemoteMealPlan): RemoteMealPlan =
        supabaseClient
            .from("meal_plans")
            .upsert(mealPlan) {
                select()
                if (mealPlan.id == null && mealPlan.clientId != null) {
                    onConflict = "client_id"
                }
            }
            .decodeSingle<RemoteMealPlan>()

    override suspend fun deleteMealPlan(remoteId: String) {
        supabaseClient.from("meal_plans").delete { filter { eq("id", remoteId) } }
    }

    override suspend fun fetchAllMealPlans(ownerId: String): List<RemoteMealPlan> =
        supabaseClient
            .from("meal_plans")
            .select { filter { eq("owner_id", ownerId) } }
            .decodeList<RemoteMealPlan>()
}
