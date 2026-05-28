package com.plusmobileapps.chefmate.aichat.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.plusmobileapps.chefmate.aichat.ChatMessage
import com.plusmobileapps.chefmate.database.AiChatMessageQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.util.DateTimeUtil
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@OptIn(ExperimentalTime::class)
@Inject
@SingleIn(AppScope::class)
class AiChatRepository(
    private val queries: AiChatMessageQueries,
    private val geminiClient: GeminiClient,
    private val dateTimeUtil: DateTimeUtil,
    @IO private val ioContext: CoroutineContext,
) {

    fun observeMessages(): Flow<List<ChatMessage>> =
        queries.getAll().asFlow().mapToList(ioContext).map { rows ->
            rows.map {
                ChatMessage(
                    id = it.id,
                    role =
                        if (it.role == ROLE_USER) ChatMessage.Role.USER else ChatMessage.Role.MODEL,
                    content = it.content,
                    isStreaming = it.isStreaming != 0L,
                )
            }
        }

    suspend fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val now = dateTimeUtil.now.toEpochMilliseconds()

        // Insert the user message in its own transaction so SQLDelight's observer fires before
        // the network round-trip starts. The model row is deferred until the first delta arrives
        // so an empty placeholder doesn't appear during the network wait.
        withContext(ioContext) {
            queries
                .insertMessage(
                    role = ROLE_USER,
                    content = trimmed,
                    createdAt = now,
                    isStreaming = 0L,
                )
                .executeAsOne()
        }

        val historyMessages =
            withContext(ioContext) {
                queries.getAll().executeAsList().map {
                    ChatMessage(
                        id = it.id,
                        role =
                            if (it.role == ROLE_USER) ChatMessage.Role.USER
                            else ChatMessage.Role.MODEL,
                        content = it.content,
                        isStreaming = false,
                    )
                }
            }

        var modelId: Long? = null
        val accumulated = StringBuilder()
        try {
            geminiClient.streamReply(historyMessages).collect { delta ->
                accumulated.append(delta)
                val content = accumulated.toString()
                withContext(ioContext) {
                    val id = modelId
                    if (id == null) {
                        modelId =
                            queries
                                .insertMessage(
                                    role = ROLE_MODEL,
                                    content = content,
                                    createdAt = dateTimeUtil.now.toEpochMilliseconds(),
                                    isStreaming = 1L,
                                )
                                .executeAsOne()
                    } else {
                        queries.updateContent(content = content, isStreaming = 1L, id = id)
                    }
                }
            }
            modelId?.let { id ->
                withContext(ioContext) {
                    queries.updateContent(
                        content = accumulated.toString(),
                        isStreaming = 0L,
                        id = id,
                    )
                }
            }
        } catch (e: Throwable) {
            val errorContent =
                if (accumulated.isEmpty()) e.message ?: "Unknown error" else accumulated.toString()
            withContext(ioContext) {
                val id = modelId
                if (id == null) {
                    queries
                        .insertMessage(
                            role = ROLE_MODEL,
                            content = errorContent,
                            createdAt = dateTimeUtil.now.toEpochMilliseconds(),
                            isStreaming = 0L,
                        )
                        .executeAsOne()
                } else {
                    queries.updateContent(content = errorContent, isStreaming = 0L, id = id)
                }
            }
            throw e
        }
    }

    suspend fun clearHistory() {
        withContext(ioContext) { queries.deleteAll() }
    }

    companion object {
        private const val ROLE_USER = "user"
        private const val ROLE_MODEL = "model"
    }
}
