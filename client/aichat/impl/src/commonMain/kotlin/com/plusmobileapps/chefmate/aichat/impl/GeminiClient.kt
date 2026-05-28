package com.plusmobileapps.chefmate.aichat.impl

import com.plusmobileapps.chefmate.aichat.ChatMessage
import com.plusmobileapps.chefmate.aichat.impl.di.GeminiHttpClient
import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GeminiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Streams Gemini's reply for the current chat history. Emits text deltas as Server-Sent Events
 * arrive so the UI can render the response progressively.
 */
interface GeminiClient {
    fun streamReply(history: List<ChatMessage>): Flow<String>
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class RealGeminiClient(@GeminiHttpClient private val httpClient: HttpClient) : GeminiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    override fun streamReply(history: List<ChatMessage>): Flow<String> = flow {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            throw GeminiException("MISSING_API_KEY")
        }
        val contents = history.map { message ->
            GeminiContent(
                role = if (message.role == ChatMessage.Role.USER) "user" else "model",
                parts = listOf(GeminiPart(text = message.content)),
            )
        }
        val request = GeminiRequest(contents = contents)

        httpClient.sse(
            urlString =
                "https://generativelanguage.googleapis.com/v1beta/models/" +
                    "$MODEL_ID:streamGenerateContent?alt=sse&key=$apiKey",
            request = {
                method = HttpMethod.Post
                contentType(ContentType.Application.Json)
                headers { append(HttpHeaders.Accept, "text/event-stream") }
                setBody(request)
            },
        ) {
            incoming.collect { event ->
                val data = event.data ?: return@collect
                if (data.isBlank() || data == "[DONE]") return@collect
                val parsed =
                    runCatching { json.decodeFromString(GeminiStreamResponse.serializer(), data) }
                        .getOrNull() ?: return@collect
                val text = parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrEmpty()) emit(text)
            }
        }
    }

    @Serializable private data class GeminiRequest(val contents: List<GeminiContent>)

    @Serializable private data class GeminiContent(val role: String, val parts: List<GeminiPart>)

    @Serializable private data class GeminiPart(val text: String)

    @Serializable
    private data class GeminiStreamResponse(val candidates: List<GeminiCandidate>? = null)

    @Serializable private data class GeminiCandidate(val content: GeminiContent? = null)

    companion object {
        private const val MODEL_ID = "gemini-2.5-flash"
    }
}
