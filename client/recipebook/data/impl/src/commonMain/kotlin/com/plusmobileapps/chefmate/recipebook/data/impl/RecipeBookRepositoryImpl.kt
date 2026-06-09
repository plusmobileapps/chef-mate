package com.plusmobileapps.chefmate.recipebook.data.impl

import app.cash.sqldelight.coroutines.asFlow
import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.database.RecipeBook as DbRecipeBook
import com.plusmobileapps.chefmate.database.RecipeBookQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.recipe.data.SyncStatus
import com.plusmobileapps.chefmate.recipebook.data.RecipeBook
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRepository
import com.plusmobileapps.chefmate.recipebook.data.impl.remote.RecipeBookRemoteDataSource
import com.plusmobileapps.chefmate.recipebook.data.impl.remote.RemoteRecipeBook
import com.plusmobileapps.chefmate.util.DateTimeUtil
import com.plusmobileapps.chefmate.util.Unique
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@SingleIn(AppScope::class)
@Inject
@ContributesBinding(AppScope::class)
class RecipeBookRepositoryImpl(
    private val db: RecipeBookQueries,
    @IO private val ioContext: CoroutineContext,
    private val dateTimeUtil: DateTimeUtil,
    private val unique: Unique,
    private val remoteDataSource: RecipeBookRemoteDataSource,
    private val authRepository: AuthenticationRepository,
    private val settings: Settings,
) : RecipeBookRepository {

    private val scope = CoroutineScope(ioContext + SupervisorJob())
    private val syncMutex = Mutex()
    private val _activeBookId = MutableStateFlow<Long?>(null)
    override val activeBookId: StateFlow<Long?> = _activeBookId.asStateFlow()

    init {
        scope.launch {
            val defaultId = ensureDefaultBookId()
            val stored = settings.getLong(KEY_ACTIVE_BOOK, NO_ACTIVE_BOOK)
            _activeBookId.value =
                if (stored != NO_ACTIVE_BOOK && bookExists(stored)) stored else defaultId
        }
        scope.launch {
            authRepository.state.collect { state ->
                if (state is AuthState.Authenticated) {
                    syncWithRemote(state.user.userId)
                }
            }
        }
    }

    override fun getRecipeBooks(): Flow<List<RecipeBook>> =
        db.getAll()
            .asFlow()
            .map { query -> query.executeAsList().map { it.toRecipeBook() } }
            .flowOn(ioContext)

    override fun getRecipeBook(id: Long): Flow<RecipeBook?> =
        db.getById(id).asFlow().map { it.executeAsOneOrNull()?.toRecipeBook() }.flowOn(ioContext)

    override suspend fun getDefaultBookId(): Long = ensureDefaultBookId()

    override suspend fun setActiveBook(id: Long) {
        settings.putLong(KEY_ACTIVE_BOOK, id)
        _activeBookId.value = id
    }

    override suspend fun createBook(name: String): RecipeBook {
        val clientId = unique.generate()
        val book =
            withContext(ioContext) {
                db.transactionWithResult {
                    db.create(
                        name = name,
                        isDefault = false,
                        createdAt = dateTimeUtil.now.toString(),
                        updatedAt = dateTimeUtil.now.toString(),
                        clientId = clientId,
                        ownerId = null,
                    )
                    val id = db.lastInsertId().executeAsOne().MAX ?: error("No last insert id")
                    db.getById(id).executeAsOne().toRecipeBook()
                }
            }
        pushAddToRemote(book.id)
        return book
    }

    override suspend fun renameBook(id: Long, name: String): RecipeBook {
        val book =
            withContext(ioContext) {
                db.transactionWithResult {
                    db.rename(name = name, updatedAt = dateTimeUtil.now.toString(), id = id)
                    db.getById(id).executeAsOne().toRecipeBook()
                }
            }
        pushUpdateToRemote(id)
        return book
    }

    override suspend fun syncAllUnsynced() {
        val authState = authRepository.state.value
        if (authState is AuthState.Authenticated) {
            syncWithRemote(authState.user.userId)
        }
    }

    override suspend fun clearLocalData() {
        withContext(ioContext) { db.deleteAll() }
    }

    /** Creates the default book if absent and returns its local id. Idempotent. */
    private suspend fun ensureDefaultBookId(): Long =
        withContext(ioContext) {
            db.getDefault().executeAsOneOrNull()?.id
                ?: db.transactionWithResult {
                    db.create(
                        name = DEFAULT_BOOK_NAME,
                        isDefault = true,
                        createdAt = dateTimeUtil.now.toString(),
                        updatedAt = dateTimeUtil.now.toString(),
                        clientId = unique.generate(),
                        ownerId = null,
                    )
                    db.lastInsertId().executeAsOne().MAX ?: error("No last insert id")
                }
        }

    private suspend fun bookExists(id: Long): Boolean =
        withContext(ioContext) { db.getById(id).executeAsOneOrNull() != null }

    private suspend fun pushAddToRemote(localId: Long) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        try {
            val entity =
                withContext(ioContext) { db.getById(localId).executeAsOneOrNull() } ?: return
            val clientId =
                entity.clientId
                    ?: unique.generate().also { newId ->
                        withContext(ioContext) { db.updateClientId(clientId = newId, id = localId) }
                    }
            val remote =
                remoteDataSource.upsertRecipeBook(
                    RemoteRecipeBook(
                        ownerId = authState.user.userId,
                        name = entity.name,
                        isDefault = entity.isDefault,
                        clientId = clientId,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt,
                    )
                )
            withContext(ioContext) { db.updateRemoteId(remoteId = remote.id, id = localId) }
        } catch (t: Throwable) {
            Logger.e(throwable = t, tag = TAG) { "pushAddToRemote failed (localId=$localId)" }
        }
    }

    private suspend fun pushUpdateToRemote(localId: Long) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        try {
            val entity =
                withContext(ioContext) { db.getById(localId).executeAsOneOrNull() } ?: return
            val remoteId = entity.remoteId ?: return
            remoteDataSource.upsertRecipeBook(
                RemoteRecipeBook(
                    id = remoteId,
                    ownerId = authState.user.userId,
                    name = entity.name,
                    isDefault = entity.isDefault,
                    clientId = entity.clientId,
                    updatedAt = entity.updatedAt,
                )
            )
            withContext(ioContext) { db.clearDirty(localId) }
        } catch (t: Throwable) {
            Logger.e(throwable = t, tag = TAG) { "pushUpdateToRemote failed (localId=$localId)" }
        }
    }

    private suspend fun syncWithRemote(userId: String) = syncMutex.withLock {
        try {
            // Retry remote deletes for any locally tombstoned books.
            val pendingDeletes = withContext(ioContext) { db.getPendingDeletes().executeAsList() }
            for (book in pendingDeletes) {
                val remoteId = book.remoteId ?: continue
                try {
                    remoteDataSource.deleteRecipeBook(remoteId)
                    withContext(ioContext) { db.delete(book.id) }
                } catch (_: Exception) {}
            }

            // Push unsynced books (no remoteId yet).
            val unsynced = withContext(ioContext) { db.getUnsynced().executeAsList() }
            for (book in unsynced) {
                try {
                    val clientId =
                        book.clientId
                            ?: unique.generate().also { newId ->
                                withContext(ioContext) {
                                    db.updateClientId(clientId = newId, id = book.id)
                                }
                            }
                    val remote =
                        remoteDataSource.upsertRecipeBook(
                            RemoteRecipeBook(
                                ownerId = userId,
                                name = book.name,
                                isDefault = book.isDefault,
                                clientId = clientId,
                                createdAt = book.createdAt,
                                updatedAt = book.updatedAt,
                            )
                        )
                    withContext(ioContext) { db.updateRemoteId(remoteId = remote.id, id = book.id) }
                } catch (_: Exception) {}
            }

            // Push dirty books (already have a remoteId).
            val dirty = withContext(ioContext) { db.getDirty().executeAsList() }
            for (book in dirty) {
                try {
                    val remoteId = book.remoteId ?: continue
                    remoteDataSource.upsertRecipeBook(
                        RemoteRecipeBook(
                            id = remoteId,
                            ownerId = userId,
                            name = book.name,
                            isDefault = book.isDefault,
                            clientId = book.clientId,
                            updatedAt = book.updatedAt,
                        )
                    )
                    withContext(ioContext) { db.clearDirty(book.id) }
                } catch (_: Exception) {}
            }

            // Pull remote books.
            val remoteBooks = remoteDataSource.fetchAllRecipeBooks(userId)
            withContext(ioContext) {
                for (remote in remoteBooks) {
                    val remoteId = remote.id ?: continue
                    if (db.getByRemoteId(remoteId).executeAsOneOrNull() != null) continue

                    // Adopt the existing remote id onto the matching local book rather than
                    // inserting a duplicate. Match by clientId first, then — for the default book —
                    // by the isDefault sentinel so two devices' "My Recipes" converge to one row.
                    val clientId = remote.clientId
                    val matchedByClientId: DbRecipeBook? =
                        if (clientId != null) {
                            db.getByClientId(clientId).executeAsOneOrNull()
                        } else {
                            null
                        }
                    val matched: DbRecipeBook? =
                        when {
                            matchedByClientId != null -> matchedByClientId
                            remote.isDefault ->
                                db.getDefault().executeAsOneOrNull()?.takeIf { it.remoteId == null }
                            else -> null
                        }
                    if (matched != null) {
                        db.updateRemoteId(remoteId = remoteId, id = matched.id)
                    } else {
                        db.createWithRemoteId(
                            name = remote.name,
                            isDefault = remote.isDefault,
                            createdAt = remote.createdAt ?: dateTimeUtil.now.toString(),
                            updatedAt = remote.updatedAt ?: dateTimeUtil.now.toString(),
                            remoteId = remoteId,
                            clientId = remote.clientId,
                            ownerId = userId,
                        )
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun DbRecipeBook.toRecipeBook(): RecipeBook {
        val syncStatus =
            when {
                isDirty -> SyncStatus.NOT_SYNCED
                remoteId != null -> SyncStatus.SYNCED
                else -> SyncStatus.NOT_SYNCED
            }
        return RecipeBook(
            id = id,
            name = name,
            isDefault = isDefault,
            syncStatus = syncStatus,
            createdAt = parseTimestamp(createdAt),
            updatedAt = parseTimestamp(updatedAt),
        )
    }

    /**
     * Books seeded by an upgrade migration's column default carry a SQLite datetime (`"2026-06-08
     * 22:11:24"` — space-separated, UTC, no offset), which [Instant.parse] rejects. Normalise that
     * shape to ISO-8601 before parsing so those rows don't crash on read.
     */
    private fun parseTimestamp(value: String): Instant =
        Instant.parse(if (value.contains('T')) value else "${value.replace(' ', 'T')}Z")

    private companion object {
        const val TAG = "RecipeBookRepositoryImpl"
        const val KEY_ACTIVE_BOOK = "recipe_active_book"
        const val NO_ACTIVE_BOOK = -1L
        const val DEFAULT_BOOK_NAME = "My Recipes"
    }
}
