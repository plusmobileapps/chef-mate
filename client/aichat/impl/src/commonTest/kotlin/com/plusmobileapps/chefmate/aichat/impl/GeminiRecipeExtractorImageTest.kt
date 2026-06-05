package com.plusmobileapps.chefmate.aichat.impl

import com.plusmobileapps.chefmate.recipe.data.RecipeExtractionError
import com.plusmobileapps.chefmate.recipe.data.RecipeExtractionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.encodeBase64
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class GeminiRecipeExtractorImageTest {

    private val imageBytes = byteArrayOf(1, 2, 3, 4)
    private var lastRequestBody: String? = null

    @Test
    fun extractFromImage_sends_image_as_inline_data_and_maps_response() = runTest {
        val extractor =
            extractorReturning(
                geminiResponse(
                    """{"title":"Pancakes","ingredients":["flour","milk"],""" +
                        """"directions":["mix","cook"],"servings":4,"prepTime":10}"""
                )
            )

        val recipe = extractor.extractFromImage(imageBytes, "image/jpeg")

        recipe.title shouldBe "Pancakes"
        recipe.ingredients shouldBe listOf("flour", "milk")
        recipe.directions shouldBe listOf("mix", "cook")
        recipe.servings shouldBe 4
        recipe.prepTime shouldBe 10

        val body = lastRequestBody!!
        body shouldContain "inlineData"
        body shouldContain "mimeType"
        body shouldContain "image/jpeg"
        body shouldContain imageBytes.encodeBase64()
    }

    @Test
    fun extractFromImage_blank_api_key_throws_missing_api_key() = runTest {
        val extractor = extractorReturning(geminiResponse("{}"), apiKey = "")

        val error =
            shouldThrow<RecipeExtractionException> {
                extractor.extractFromImage(imageBytes, "image/jpeg")
            }
        error.error shouldBe RecipeExtractionError.MISSING_API_KEY
    }

    @Test
    fun extractFromImage_no_candidates_throws_empty_response() = runTest {
        val extractor = extractorReturning("""{"candidates":[]}""")

        val error =
            shouldThrow<RecipeExtractionException> {
                extractor.extractFromImage(imageBytes, "image/jpeg")
            }
        error.error shouldBe RecipeExtractionError.EMPTY_RESPONSE
    }

    @Test
    fun extractFromImage_unparseable_payload_throws_malformed_json() = runTest {
        val extractor = extractorReturning(geminiResponse("not valid json"))

        val error =
            shouldThrow<RecipeExtractionException> {
                extractor.extractFromImage(imageBytes, "image/jpeg")
            }
        error.error shouldBe RecipeExtractionError.MALFORMED_JSON
    }

    @Test
    fun extractFromImage_missing_ingredients_throws_incomplete_recipe() = runTest {
        val extractor =
            extractorReturning(
                geminiResponse("""{"title":"Pancakes","ingredients":[],"directions":["cook"]}""")
            )

        val error =
            shouldThrow<RecipeExtractionException> {
                extractor.extractFromImage(imageBytes, "image/jpeg")
            }
        error.error shouldBe RecipeExtractionError.INCOMPLETE_RECIPE
    }

    @Test
    fun extractFromImage_http_failure_throws_request_failed() = runTest {
        val extractor = extractorReturning("oops", status = HttpStatusCode.InternalServerError)

        val error =
            shouldThrow<RecipeExtractionException> {
                extractor.extractFromImage(imageBytes, "image/jpeg")
            }
        error.error shouldBe RecipeExtractionError.REQUEST_FAILED
    }

    /** Wraps [recipeJson] (the structured-output payload) in Gemini's candidate envelope. */
    private fun geminiResponse(recipeJson: String): String =
        buildJsonObject {
                putJsonArray("candidates") {
                    addJsonObject {
                        putJsonObject("content") {
                            put("role", "model")
                            putJsonArray("parts") { addJsonObject { put("text", recipeJson) } }
                        }
                    }
                }
            }
            .toString()

    private fun extractorReturning(
        responseBody: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        apiKey: String = "test-key",
    ): RealGeminiRecipeExtractor {
        val engine = MockEngine { request ->
            lastRequestBody = (request.body as TextContent).text
            respond(
                content = responseBody,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            encodeDefaults = false
                        }
                    )
                }
            }
        return RealGeminiRecipeExtractor(client, apiKey)
    }
}
