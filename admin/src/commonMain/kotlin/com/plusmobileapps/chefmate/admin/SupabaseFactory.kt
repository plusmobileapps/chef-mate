package com.plusmobileapps.chefmate.admin

import com.plusmobileapps.chefmate.admin.buildconfig.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Builds the Supabase client for the admin app from the build-time anon key. The anon key is safe
 * to ship in the web bundle: admin writes are gated by the `admins` RLS policy, not key secrecy.
 */
fun createAdminSupabaseClient(): SupabaseClient =
    createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY,
    ) {
        install(Auth)
        install(Postgrest)
    }
