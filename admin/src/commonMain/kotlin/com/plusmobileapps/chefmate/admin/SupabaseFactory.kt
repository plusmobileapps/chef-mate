package com.plusmobileapps.chefmate.admin

import com.plusmobileapps.chefmate.admin.buildconfig.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

/**
 * Builds the Supabase client for the admin app from the build-time anon key. The anon key is safe
 * to ship in the web bundle: admin writes are gated by the `admins` RLS policy, not key secrecy.
 */
fun createAdminSupabaseClient(): SupabaseClient =
    createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY,
    ) {
        // The library default (encodeDefaults = false) drops any field whose value matches its
        // Kotlin default from the JSON body — e.g. AdminFeatureFlag.rolloutPercent defaults to
        // 100, so setting rollout to exactly 100% silently omitted rollout_percent from
        // insert/upsert requests, leaving the old value in place on update. Always encode
        // defaults so every field is sent explicitly.
        defaultSerializer =
            KotlinXSerializer(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            )
        install(Auth)
        install(Postgrest)
    }
