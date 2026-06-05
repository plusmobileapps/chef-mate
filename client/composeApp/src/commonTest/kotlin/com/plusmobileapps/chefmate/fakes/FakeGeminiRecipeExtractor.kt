package com.plusmobileapps.chefmate.fakes

import com.plusmobileapps.chefmate.aichat.ChatMessage
import com.plusmobileapps.chefmate.aichat.impl.GeminiExtractionException
import com.plusmobileapps.chefmate.aichat.impl.GeminiRecipeExtractor
import com.plusmobileapps.chefmate.aichat.impl.RealGeminiRecipeExtractor
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.recipe.data.RecipeExtractionException
import com.plusmobileapps.chefmate.recipe.data.RecipeImageExtractor
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

/**
 * Test stand-in for both extraction entry points backed by [RealGeminiRecipeExtractor]: the chat
 * text extractor ([GeminiRecipeExtractor]) and the image extractor ([RecipeImageExtractor]).
 * Returns the staged [response] or throws so tests can exercise the error banner.
 */
@SingleIn(AppScope::class)
@Inject
@ContributesBinding(
    scope = AppScope::class,
    binding = binding<GeminiRecipeExtractor>(),
    replaces = [RealGeminiRecipeExtractor::class],
)
@ContributesBinding(
    scope = AppScope::class,
    binding = binding<RecipeImageExtractor>(),
    replaces = [RealGeminiRecipeExtractor::class],
)
class FakeGeminiRecipeExtractor : GeminiRecipeExtractor, RecipeImageExtractor {

    var response: ExtractedRecipeData? = null

    override suspend fun extract(history: List<ChatMessage>): ExtractedRecipeData =
        response ?: throw GeminiExtractionException("NO_FAKE_RESPONSE")

    override suspend fun extractFromImage(bytes: ByteArray, mimeType: String): ExtractedRecipeData =
        response ?: throw RecipeExtractionException("NO_FAKE_RESPONSE")
}
