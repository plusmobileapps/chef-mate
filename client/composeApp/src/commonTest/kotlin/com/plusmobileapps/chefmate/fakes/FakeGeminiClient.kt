package com.plusmobileapps.chefmate.fakes

import com.plusmobileapps.chefmate.aichat.ChatMessage
import com.plusmobileapps.chefmate.aichat.impl.GeminiClient
import com.plusmobileapps.chefmate.aichat.impl.RealGeminiClient
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Test stand-in for [GeminiClient] that returns whatever [deltas] are set before [streamReply] is
 * collected. Defaults to a single canned reply so unstaged tests still get something useful.
 */
@SingleIn(AppScope::class)
@Inject
@ContributesBinding(scope = AppScope::class, replaces = [RealGeminiClient::class])
class FakeGeminiClient : GeminiClient {

    var deltas: List<String> = listOf("Here's a recipe for you!")

    override fun streamReply(history: List<ChatMessage>): Flow<String> = flow {
        deltas.forEach { emit(it) }
    }
}
