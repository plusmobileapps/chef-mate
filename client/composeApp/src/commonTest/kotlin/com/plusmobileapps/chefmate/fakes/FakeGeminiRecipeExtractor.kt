package com.plusmobileapps.chefmate.fakes

import com.plusmobileapps.chefmate.aichat.ChatMessage
import com.plusmobileapps.chefmate.aichat.impl.GeminiExtractionException
import com.plusmobileapps.chefmate.aichat.impl.GeminiRecipeExtractor
import com.plusmobileapps.chefmate.aichat.impl.RealGeminiRecipeExtractor
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Test stand-in for [GeminiRecipeExtractor] that returns the staged [response] or throws
 * [GeminiExtractionException] when none is set so tests can exercise the error banner.
 */
@SingleIn(AppScope::class)
@Inject
@ContributesBinding(scope = AppScope::class, replaces = [RealGeminiRecipeExtractor::class])
class FakeGeminiRecipeExtractor : GeminiRecipeExtractor {

    var response: ExtractedRecipeData? = null

    override suspend fun extract(history: List<ChatMessage>): ExtractedRecipeData =
        response ?: throw GeminiExtractionException("NO_FAKE_RESPONSE")
}
