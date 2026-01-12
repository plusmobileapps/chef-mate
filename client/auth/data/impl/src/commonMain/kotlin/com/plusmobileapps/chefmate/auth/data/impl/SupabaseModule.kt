package com.plusmobileapps.chefmate.auth.data.impl

import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
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
        Napier.d("Initializing Supabase client")
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY,
        ) {
            install(Auth) {
                scheme = "chefmate"
                host = "auth"
            }
        }
    }
}
