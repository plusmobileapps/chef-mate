package com.plusmobileapps.chefmate.aichat.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.plusmobileapps.chefmate.aichat.AiChatConversation
import com.plusmobileapps.chefmate.aichat.AiChatLocalDataCleaner
import com.plusmobileapps.chefmate.aichat.ChatMessage
import com.plusmobileapps.chefmate.database.AiChatConversationQueries
import com.plusmobileapps.chefmate.database.AiChatMessageQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.util.DateTimeUtil
import dev.zacsweers.metro.ContributesBinding
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
@ContributesBinding(AppScope::class)
class AiChatRepository(
    private val messageQueries: AiChatMessageQueries,
    private val conversationQueries: AiChatConversationQueries,
    private val geminiClient: GeminiClient,
    private val dateTimeUtil: DateTimeUtil,
    @IO private val ioContext: CoroutineContext,
) : AiChatLocalDataCleaner {

    fun observeConversations(): Flow<List<AiChatConversation>> =
        conversationQueries.observeAll().asFlow().mapToList(ioContext).map { rows ->
            rows.map {
                AiChatConversation(
                    id = it.id,
                    title = it.title,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            }
        }

    /**
     * The id of the most recently updated conversation started from [recipeId], or null if none
     * exists yet. Read synchronously (a single indexed lookup) so the chat host can decide up front
     * whether to reopen a recipe's prior conversation or start a fresh one.
     */
    fun latestConversationIdForRecipe(recipeId: Long): Long? =
        conversationQueries.getMostRecentForRecipe(recipeId).executeAsOneOrNull()?.id

    /** The recipe [conversationId] was started from, if any. */
    suspend fun recipeContextIdFor(conversationId: Long): Long? =
        withContext(ioContext) {
            conversationQueries.getById(conversationId).executeAsOneOrNull()?.recipeContextId
        }

    fun observeMessages(conversationId: Long): Flow<List<ChatMessage>> =
        messageQueries.getAllForConversation(conversationId).asFlow().mapToList(ioContext).map {
            rows ->
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

    /**
     * Sends [text] in [conversationId]. If [conversationId] is null a new conversation is created
     * (with [text] used as its title) before the user row is inserted. Returns the id of the
     * conversation the message was sent into.
     *
     * [onConversationStarted] is invoked with the active conversation id as soon as the user row is
     * persisted — before the (potentially long) model stream begins. Callers observing messages by
     * conversation id should switch to it here so a freshly created conversation's first user
     * message appears immediately instead of only after the reply finishes streaming.
     */
    suspend fun sendMessage(
        conversationId: Long?,
        text: String,
        recipeContextId: Long? = null,
        recipeContextPreamble: String? = null,
        onConversationStarted: (Long) -> Unit = {},
    ): Long? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return conversationId

        val now = dateTimeUtil.now.toEpochMilliseconds()

        val activeConversationId: Long =
            withContext(ioContext) {
                conversationId
                    ?: conversationQueries
                        .insertConversation(
                            title = trimmed.take(TITLE_MAX_LENGTH),
                            createdAt = now,
                            updatedAt = now,
                            recipeContextId = recipeContextId,
                        )
                        .executeAsOne()
            }

        // Insert the user message in its own transaction so SQLDelight's observer fires before
        // the network round-trip starts. The model row is deferred until the first delta arrives
        // so an empty placeholder doesn't appear during the network wait.
        withContext(ioContext) {
            messageQueries
                .insertMessage(
                    conversationId = activeConversationId,
                    role = ROLE_USER,
                    content = trimmed,
                    createdAt = now,
                    isStreaming = 0L,
                )
                .executeAsOne()
            conversationQueries.touchConversation(updatedAt = now, id = activeConversationId)
        }

        // The user row is committed; let the caller start observing this conversation now so the
        // message shows up before the model reply streams back.
        onConversationStarted(activeConversationId)

        val historyMessages =
            withContext(ioContext) {
                messageQueries.getAllForConversation(activeConversationId).executeAsList().map {
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

        // Fold the recipe context into the first user turn (rather than sending it as an extra
        // turn) so the request keeps strict user/model alternation while still grounding the reply.
        // It only travels with the request — the persisted conversation is untouched.
        val outgoingMessages =
            if (recipeContextPreamble != null && historyMessages.isNotEmpty()) {
                val first = historyMessages.first()
                listOf(first.copy(content = recipeContextPreamble + "\n\n" + first.content)) +
                    historyMessages.drop(1)
            } else {
                historyMessages
            }

        var modelId: Long? = null
        val accumulated = StringBuilder()
        // Persisting every token re-runs the message query and recomposes the whole chat per token,
        // which makes the streaming reply flash. Coalesce in-flight updates to at most one write
        // per
        // [STREAM_WRITE_INTERVAL_MS]; the user-facing typewriter smooths the larger jumps, and the
        // final flush below always persists the complete content.
        var lastWriteAt = 0L
        try {
            geminiClient.streamReply(outgoingMessages).collect { delta ->
                accumulated.append(delta)
                val content = accumulated.toString()
                val id = modelId
                if (id == null) {
                    // Insert the row on the first delta so the reply bubble appears immediately.
                    modelId =
                        withContext(ioContext) {
                            messageQueries
                                .insertMessage(
                                    conversationId = activeConversationId,
                                    role = ROLE_MODEL,
                                    content = content,
                                    createdAt = dateTimeUtil.now.toEpochMilliseconds(),
                                    isStreaming = 1L,
                                )
                                .executeAsOne()
                        }
                    lastWriteAt = dateTimeUtil.now.toEpochMilliseconds()
                } else {
                    val now = dateTimeUtil.now.toEpochMilliseconds()
                    if (now - lastWriteAt >= STREAM_WRITE_INTERVAL_MS) {
                        withContext(ioContext) {
                            messageQueries.updateContent(
                                content = content,
                                isStreaming = 1L,
                                id = id,
                            )
                        }
                        lastWriteAt = now
                    }
                }
            }
            modelId?.let { id ->
                withContext(ioContext) {
                    messageQueries.updateContent(
                        content = accumulated.toString(),
                        isStreaming = 0L,
                        id = id,
                    )
                    conversationQueries.touchConversation(
                        updatedAt = dateTimeUtil.now.toEpochMilliseconds(),
                        id = activeConversationId,
                    )
                }
            }
        } catch (e: Throwable) {
            val errorContent =
                if (accumulated.isEmpty()) e.message ?: "Unknown error" else accumulated.toString()
            withContext(ioContext) {
                val id = modelId
                if (id == null) {
                    messageQueries
                        .insertMessage(
                            conversationId = activeConversationId,
                            role = ROLE_MODEL,
                            content = errorContent,
                            createdAt = dateTimeUtil.now.toEpochMilliseconds(),
                            isStreaming = 0L,
                        )
                        .executeAsOne()
                } else {
                    messageQueries.updateContent(content = errorContent, isStreaming = 0L, id = id)
                }
            }
            throw e
        }
        return activeConversationId
    }

    suspend fun deleteConversation(conversationId: Long) {
        withContext(ioContext) { conversationQueries.deleteConversation(conversationId) }
    }

    suspend fun deleteAllConversations() = clearLocalData()

    override suspend fun clearLocalData() {
        withContext(ioContext) { conversationQueries.deleteAllConversations() }
    }

    companion object {
        private const val ROLE_USER = "user"
        private const val ROLE_MODEL = "model"
        private const val TITLE_MAX_LENGTH = 120
        /** Minimum gap between in-flight streaming writes, to coalesce per-token DB churn. */
        private const val STREAM_WRITE_INTERVAL_MS = 100L
    }
}
