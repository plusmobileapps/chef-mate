package com.plusmobileapps.chefmate.auth.data.impl

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@SingleIn(AppScope::class)
@ContributesTo(AppScope::class)
interface SupabaseModule {

    @SingleIn(AppScope::class)
    @Provides
    fun provideSupabaseClient(): SupabaseClient {
        // TODO: Replace these with your actual Supabase project credentials
        // You can find these in your Supabase dashboard at:
        // https://app.supabase.com/project/YOUR_PROJECT_ID/settings/api
        val supabaseUrl = "https://your-project-id.supabase.co" // TODO: Replace with your Supabase URL
        val supabaseKey = "your-anon-public-key" // TODO: Replace with your Supabase anon/public key

        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey,
        ) {
            install(Auth) {
                flowType = FlowType.PKCE
                scheme = "chefmate" // TODO: Optionally customize this for deep linking
                host = "auth"
            }
        }
    }
}
