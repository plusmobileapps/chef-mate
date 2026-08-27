package com.plusmobileapps.chefmate.auth.data.impl

import com.plusmobileapps.chefmate.Environment
import com.plusmobileapps.chefmate.EnvironmentProvider
import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

@SingleIn(AppScope::class)
@ContributesTo(AppScope::class)
interface SupabaseModule {
    // httpConfig is marked internal by supabase-kt, but it is the only hook that reaches the
    // shared HttpClient every plugin sends through — which is exactly the layer a 401 retry belongs
    // at.
    @OptIn(SupabaseInternal::class)
    @SingleIn(AppScope::class)
    @Provides
    fun provideSupabaseClient(environmentProvider: EnvironmentProvider): SupabaseClient {
        // The client is bound to the env at first injection — switching env at runtime requires
        // an app restart, which the dev-settings UI prompts for. FAKE falls back to PROD URL
        // because we still need a valid client to construct; remote calls are gated by sign-in
        // (the env switch signs the user out), so seeded FAKE data stays local until someone
        // signs back in.
        val (url, key) =
            when (environmentProvider.environment.value) {
                Environment.TESTING ->
                    BuildConfig.SUPABASE_TESTING_URL to BuildConfig.SUPABASE_TESTING_KEY
                Environment.PROD,
                Environment.FAKE -> BuildConfig.SUPABASE_PROD_URL to BuildConfig.SUPABASE_PROD_KEY
            }

        // [ExpiredTokenRetry] has to refresh the session of the very client it is being installed
        // into, so it reads the client back out of this holder, which is filled the moment
        // createSupabaseClient returns — always before anything can issue a request through it.
        var client: SupabaseClient? = null
        val refreshGuard =
            ConcurrentRefreshGuard(
                currentToken = { client?.auth?.currentAccessTokenOrNull() },
                refresh = { client?.auth?.refreshCurrentSession() },
            )

        return createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
                install(Auth)
                install(Postgrest)
                install(Realtime)
                install(Storage)
                install(Functions)
                httpConfig {
                    install(ExpiredTokenRetry) {
                        refreshToken = { usedToken -> refreshGuard.tokenAfterRefresh(usedToken) }
                    }
                }
            }
            .also { client = it }
    }
}
