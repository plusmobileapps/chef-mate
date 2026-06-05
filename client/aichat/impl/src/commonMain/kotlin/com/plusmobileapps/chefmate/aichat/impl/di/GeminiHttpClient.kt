package com.plusmobileapps.chefmate.aichat.impl.di

import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.Qualifier
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Qualifier for the single [HttpClient] shared by every Gemini integration in the chat module.
 *
 * Both [com.plusmobileapps.chefmate.aichat.impl.GeminiClient] (streaming `:streamGenerateContent`)
 * and [com.plusmobileapps.chefmate.aichat.impl.GeminiRecipeExtractor] (one-shot `:generateContent`)
 * resolve their client via this qualifier so they share the same connection pool, JSON config, and
 * installed plugins.
 */
@Qualifier
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE,
)
annotation class GeminiHttpClient

/**
 * Qualifier for the Gemini API key. Provided from [BuildConfig] but injected (rather than read
 * directly) so the Gemini clients can be unit-tested with a fixed key instead of depending on the
 * build-time value.
 */
@Qualifier
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE,
)
annotation class GeminiApiKey

@ContributesTo(AppScope::class)
interface GeminiHttpClientComponent {

    @Provides
    @SingleIn(AppScope::class)
    @GeminiHttpClient
    fun providesGeminiHttpClient(): HttpClient = HttpClient {
        install(SSE)
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = false
                }
            )
        }
    }

    @Provides @GeminiApiKey fun providesGeminiApiKey(): String = BuildConfig.GEMINI_API_KEY
}
