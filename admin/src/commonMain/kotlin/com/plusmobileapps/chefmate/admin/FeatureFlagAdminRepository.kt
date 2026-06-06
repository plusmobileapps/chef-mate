package com.plusmobileapps.chefmate.admin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

/**
 * CRUD operations for feature flags. All writes are gated server-side by the `admins` RLS policy.
 */
interface FeatureFlagAdminRepository {
    suspend fun list(includeArchived: Boolean): List<AdminFeatureFlag>

    suspend fun create(flag: AdminFeatureFlag)

    suspend fun update(flag: AdminFeatureFlag)

    suspend fun setArchived(key: String, archived: Boolean)

    suspend fun delete(key: String)
}

class SupabaseFeatureFlagAdminRepository(private val client: SupabaseClient) :
    FeatureFlagAdminRepository {

    override suspend fun list(includeArchived: Boolean): List<AdminFeatureFlag> =
        client
            .from(TABLE)
            .select(COLUMNS) {
                if (!includeArchived) filter { eq("archived", false) }
                order(
                    column = "key",
                    order = io.github.jan.supabase.postgrest.query.Order.ASCENDING,
                )
            }
            .decodeList()

    // Insert (not upsert) so creating a key that already exists surfaces a conflict error the UI
    // can show, rather than silently overwriting an existing flag.
    override suspend fun create(flag: AdminFeatureFlag) {
        client.from(TABLE).insert(flag)
    }

    // Upsert keyed on the PK: the row always exists when editing, and the DB trigger bumps
    // `updated_at`. `created_at` is absent from the payload, so Postgres preserves it.
    override suspend fun update(flag: AdminFeatureFlag) {
        client.from(TABLE).upsert(flag)
    }

    override suspend fun setArchived(key: String, archived: Boolean) {
        client.from(TABLE).update({ set("archived", archived) }) { filter { eq("key", key) } }
    }

    override suspend fun delete(key: String) {
        client.from(TABLE).delete { filter { eq("key", key) } }
    }

    private companion object {
        const val TABLE = "feature_flags"
        // Explicit columns matching AdminFeatureFlag so admin-only audit columns
        // (created_at/updated_at) never reach strict decoding.
        val COLUMNS =
            Columns.list(
                "key",
                "value_type",
                "value",
                "enabled",
                "rollout_percent",
                "platforms",
                "min_version",
                "max_version",
                "user_ids",
                "archived",
                "description",
            )
    }
}
