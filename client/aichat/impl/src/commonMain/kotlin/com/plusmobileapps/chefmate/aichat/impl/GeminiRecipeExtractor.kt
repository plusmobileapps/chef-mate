package com.plusmobileapps.chefmate.aichat.impl

import com.plusmobileapps.chefmate.aichat.ChatMessage
import com.plusmobileapps.chefmate.aichat.impl.di.GeminiHttpClient
import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class GeminiExtractionException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

/**
 * One-shot Gemini call that turns the current chat history into an [ExtractedRecipeData] using
 * structured-output (`responseSchema` + `responseMimeType=application/json`). Used by the "Add
 * recipe" pill so the user lands in the existing Edit Recipe → CreateFromExtracted flow.
 */
interface GeminiRecipeExtractor {
    suspend fun extract(history: List<ChatMessage>): ExtractedRecipeData
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class RealGeminiRecipeExtractor(@GeminiHttpClient private val httpClient: HttpClient) :
    GeminiRecipeExtractor {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    override suspend fun extract(history: List<ChatMessage>): ExtractedRecipeData {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) throw GeminiExtractionException("MISSING_API_KEY")

        val contents =
            (history + extractionInstruction()).map { message ->
                GeminiContent(
                    role = if (message.role == ChatMessage.Role.USER) "user" else "model",
                    parts = listOf(GeminiPart(text = message.content)),
                )
            }
        val request = GeminiRequest(contents = contents, generationConfig = recipeGenerationConfig)

        val response: GeminiResponse =
            try {
                httpClient
                    .post(
                        "https://generativelanguage.googleapis.com/v1beta/models/" +
                            "$MODEL_ID:generateContent?key=$apiKey"
                    ) {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                    .body<GeminiResponse>()
            } catch (e: Throwable) {
                throw GeminiExtractionException("REQUEST_FAILED", e)
            }

        val payload =
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw GeminiExtractionException("EMPTY_RESPONSE")

        val recipe =
            runCatching { json.decodeFromString(RecipeJson.serializer(), payload) }
                .getOrElse { throw GeminiExtractionException("MALFORMED_JSON", it) }

        if (recipe.title.isBlank() || recipe.ingredients.isEmpty() || recipe.directions.isEmpty()) {
            throw GeminiExtractionException("INCOMPLETE_RECIPE")
        }

        return ExtractedRecipeData(
            title = recipe.title,
            description = recipe.description,
            ingredients = recipe.ingredients,
            directions = recipe.directions,
            imageUrl = null,
            sourceUrl = "",
            servings = recipe.servings,
            prepTime = recipe.prepTime,
            cookTime = recipe.cookTime,
            totalTime = recipe.totalTime,
            calories = recipe.calories,
        )
    }

    private fun extractionInstruction(): ChatMessage =
        ChatMessage(
            id = -1L,
            role = ChatMessage.Role.USER,
            content =
                "Extract the recipe you most recently described in this conversation as JSON " +
                    "matching the response schema. If multiple recipes were discussed, return " +
                    "only the most recent one. Use minutes for any time fields.",
            isStreaming = false,
        )

    @Serializable
    private data class GeminiRequest(
        val contents: List<GeminiContent>,
        val generationConfig: JsonObject,
    )

    @Serializable private data class GeminiContent(val role: String, val parts: List<GeminiPart>)

    @Serializable private data class GeminiPart(val text: String)

    @Serializable private data class GeminiResponse(val candidates: List<GeminiCandidate>? = null)

    @Serializable private data class GeminiCandidate(val content: GeminiContent? = null)

    @Serializable
    private data class RecipeJson(
        val title: String,
        val description: String? = null,
        val ingredients: List<String> = emptyList(),
        val directions: List<String> = emptyList(),
        val servings: Int? = null,
        @SerialName("prepTime") val prepTime: Int? = null,
        @SerialName("cookTime") val cookTime: Int? = null,
        @SerialName("totalTime") val totalTime: Int? = null,
        val calories: Int? = null,
    )

    companion object {
        private const val MODEL_ID = "gemini-2.5-flash"

        private val recipeGenerationConfig: JsonObject = buildJsonObject {
            put("responseMimeType", JsonPrimitive("application/json"))
            put(
                "responseSchema",
                buildJsonObject {
                    put("type", JsonPrimitive("OBJECT"))
                    put(
                        "properties",
                        buildJsonObject {
                            put("title", stringSchema())
                            put("description", stringSchema())
                            put("ingredients", arrayOfStrings())
                            put("directions", arrayOfStrings())
                            put("servings", integerSchema())
                            put("prepTime", integerSchema("minutes"))
                            put("cookTime", integerSchema("minutes"))
                            put("totalTime", integerSchema("minutes"))
                            put("calories", integerSchema())
                        },
                    )
                    put(
                        "required",
                        buildJsonArray {
                            add(JsonPrimitive("title"))
                            add(JsonPrimitive("ingredients"))
                            add(JsonPrimitive("directions"))
                        },
                    )
                },
            )
        }

        private fun stringSchema(): JsonObject = buildJsonObject {
            put("type", JsonPrimitive("STRING"))
        }

        private fun integerSchema(description: String? = null): JsonObject = buildJsonObject {
            put("type", JsonPrimitive("INTEGER"))
            if (description != null) put("description", JsonPrimitive(description))
        }

        private fun arrayOfStrings(): JsonObject = buildJsonObject {
            put("type", JsonPrimitive("ARRAY"))
            put("items", stringSchema())
        }
    }
}
