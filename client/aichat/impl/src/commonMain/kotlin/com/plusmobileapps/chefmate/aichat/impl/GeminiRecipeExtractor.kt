package com.plusmobileapps.chefmate.aichat.impl

import com.plusmobileapps.chefmate.aichat.ChatMessage
import com.plusmobileapps.chefmate.aichat.impl.di.AiChatFunctionConfig
import com.plusmobileapps.chefmate.aichat.impl.di.GeminiHttpClient
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.recipe.data.RecipeExtractionError
import com.plusmobileapps.chefmate.recipe.data.RecipeExtractionException
import com.plusmobileapps.chefmate.recipe.data.RecipeImageExtractor
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.util.encodeBase64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

/**
 * One-shot Gemini call that turns the current chat history into an [ExtractedRecipeData] using
 * structured-output (`responseSchema` + `responseMimeType=application/json`). Used by the "Add
 * recipe" pill so the user lands in the existing Edit Recipe → CreateFromExtracted flow.
 */
interface GeminiRecipeExtractor {
    suspend fun extract(history: List<ChatMessage>): ExtractedRecipeData
}

/**
 * Gemini-backed extractor for both entry points: text history ([GeminiRecipeExtractor.extract]) and
 * a single image ([RecipeImageExtractor.extractFromImage], used by the chat photo-attach button and
 * the standalone "Scan from photo" flow). Both share the same structured-output request and
 * parsing; only the request `contents` differ.
 *
 * Requests go through the `ai-chat` Supabase Edge Function (which holds the Gemini API key) rather
 * than Gemini directly. The function returns Gemini's `:generateContent` JSON verbatim, so the
 * `candidates` parsing below is unchanged from the direct-to-Gemini days.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<GeminiRecipeExtractor>())
@ContributesBinding(AppScope::class, binding = binding<RecipeImageExtractor>())
class RealGeminiRecipeExtractor(
    @GeminiHttpClient private val httpClient: HttpClient,
    private val functionConfig: AiChatFunctionConfig,
) : GeminiRecipeExtractor, RecipeImageExtractor {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    override suspend fun extract(history: List<ChatMessage>): ExtractedRecipeData {
        val contents =
            (history + extractionInstruction()).map { message ->
                GeminiContent(
                    role = if (message.role == ChatMessage.Role.USER) "user" else "model",
                    parts = listOf(GeminiPart(text = message.content)),
                )
            }
        return generate(contents)
    }

    override suspend fun extractFromImage(bytes: ByteArray, mimeType: String): ExtractedRecipeData {
        val contents =
            listOf(
                GeminiContent(
                    role = "user",
                    parts =
                        listOf(
                            GeminiPart(
                                inlineData =
                                    GeminiInlineData(
                                        mimeType = mimeType,
                                        data = bytes.encodeBase64(),
                                    )
                            ),
                            GeminiPart(text = IMAGE_EXTRACTION_INSTRUCTION),
                        ),
                )
            )
        return generate(contents)
    }

    /**
     * Shared structured-output call for both entry points. Failures surface as a
     * [RecipeExtractionException] carrying a typed [RecipeExtractionError].
     */
    private suspend fun generate(contents: List<GeminiContent>): ExtractedRecipeData {
        val request =
            ChatRequest(
                stream = false,
                contents = contents,
                generationConfig = recipeGenerationConfig,
            )
        val token = functionConfig.accessToken()

        val response: GeminiResponse =
            try {
                val httpResponse =
                    httpClient.post(functionConfig.functionUrl) {
                        contentType(ContentType.Application.Json)
                        headers {
                            append("apikey", functionConfig.anonKey)
                            append(HttpHeaders.Authorization, "Bearer $token")
                        }
                        setBody(request)
                    }
                // The edge function returns 503 when the server-side Gemini key isn't configured;
                // surface that as the same "no API key" state the UI showed when the key was a
                // build variable. Any other non-2xx is a generic request failure.
                if (httpResponse.status == HttpStatusCode.ServiceUnavailable) {
                    throw RecipeExtractionException(RecipeExtractionError.MISSING_API_KEY)
                }
                if (!httpResponse.status.isSuccess()) {
                    throw RecipeExtractionException(RecipeExtractionError.REQUEST_FAILED)
                }
                httpResponse.body<GeminiResponse>()
            } catch (e: RecipeExtractionException) {
                throw e
            } catch (e: Throwable) {
                throw RecipeExtractionException(RecipeExtractionError.REQUEST_FAILED, e)
            }

        val payload =
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw RecipeExtractionException(RecipeExtractionError.EMPTY_RESPONSE)

        val recipe = runCatching {
            json.decodeFromString(RecipeJson.serializer(), payload)
        }
            .getOrElse {
                throw RecipeExtractionException(RecipeExtractionError.MALFORMED_JSON, it)
            }

        if (recipe.title.isBlank() || recipe.ingredients.isEmpty() || recipe.directions.isEmpty()) {
            throw RecipeExtractionException(RecipeExtractionError.INCOMPLETE_RECIPE)
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
    private data class ChatRequest(
        val stream: Boolean,
        val contents: List<GeminiContent>,
        val generationConfig: JsonObject,
    )

    @Serializable private data class GeminiContent(val role: String, val parts: List<GeminiPart>)

    @Serializable
    private data class GeminiPart(
        val text: String? = null,
        @SerialName("inlineData") val inlineData: GeminiInlineData? = null,
    )

    @Serializable
    private data class GeminiInlineData(
        @SerialName("mimeType") val mimeType: String,
        val data: String,
    )

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
        private const val IMAGE_EXTRACTION_INSTRUCTION =
            "Extract the recipe shown in this image as JSON matching the response schema. The " +
                "image may be a cookbook page, food label, screenshot, or handwritten card. Use " +
                "minutes for any time fields. If multiple recipes are visible, return the most " +
                "prominent one."

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
