package com.plusmobileapps.chefmate.grocery.data.impl

import app.cash.sqldelight.coroutines.asFlow
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.database.GroceryAutocompleteItem as DbItem
import com.plusmobileapps.chefmate.database.GroceryAutocompleteItemQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.grocery.data.GroceryAutocompleteItem
import com.plusmobileapps.chefmate.grocery.data.GroceryAutocompleteRepository
import com.plusmobileapps.chefmate.grocery.data.impl.remote.GroceryAutocompleteRemoteDataSource
import com.plusmobileapps.chefmate.grocery.data.impl.remote.RemoteGroceryAutocompleteItem
import com.plusmobileapps.chefmate.util.Unique
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@SingleIn(AppScope::class)
@Inject
@ContributesBinding(AppScope::class)
class GroceryAutocompleteRepositoryImpl(
    private val db: GroceryAutocompleteItemQueries,
    @IO private val ioContext: CoroutineContext,
    private val unique: Unique,
    private val remoteDataSource: GroceryAutocompleteRemoteDataSource,
    private val authRepository: AuthenticationRepository,
) : GroceryAutocompleteRepository {

    private val scope = CoroutineScope(ioContext + SupervisorJob())
    private val syncMutex = Mutex()

    init {
        scope.launch {
            authRepository.state.collect { state ->
                if (state is AuthState.Authenticated) {
                    syncWithRemote(state.user.userId)
                }
            }
        }
    }

    override fun observeItems(): Flow<List<GroceryAutocompleteItem>> =
        db.getAll()
            .asFlow()
            .map { it.executeAsList().map { row -> row.toItem() } }
            .flowOn(ioContext)

    override suspend fun addItem(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val ownerId = authRepository.state.value.userIdOrNull()
        val clientId = unique.generate()
        // The unique COLLATE NOCASE index + INSERT OR IGNORE dedups case-insensitively, so read the
        // resulting row back to learn whether this was a fresh insert (worth pushing) or a no-op.
        val row =
            withContext(ioContext) {
                db.transactionWithResult {
                    db.insertOrIgnore(name = trimmed, clientId = clientId, ownerId = ownerId)
                    db.getByName(trimmed).executeAsOneOrNull()
                }
            }
        // Only push rows that aren't already synced; a pre-existing synced row is a no-op here.
        if (row != null && row.remoteId == null) pushAddToRemote(row.id)
    }

    override suspend fun deleteItem(id: Long) {
        val entity =
            withContext(ioContext) {
                val entity = db.getById(id).executeAsOneOrNull()
                db.deleteById(id)
                entity
            }
        entity?.remoteId?.let { remoteId ->
            scope.launch {
                try {
                    remoteDataSource.deleteItem(remoteId)
                } catch (_: Exception) {}
            }
        }
    }

    override suspend fun clearLocalData() {
        withContext(ioContext) { db.deleteAll() }
    }

    private fun pushAddToRemote(localId: Long) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        scope.launch {
            try {
                val entity =
                    withContext(ioContext) { db.getById(localId).executeAsOneOrNull() }
                        ?: return@launch
                if (entity.remoteId != null) return@launch
                val clientId =
                    entity.clientId
                        ?: unique.generate().also { newId ->
                            withContext(ioContext) {
                                db.updateClientId(clientId = newId, id = localId)
                            }
                        }
                val remote =
                    remoteDataSource.upsertItem(
                        RemoteGroceryAutocompleteItem(
                            ownerId = authState.user.userId,
                            name = entity.name,
                            clientId = clientId,
                        )
                    )
                remote.id?.let { remoteId ->
                    withContext(ioContext) { db.updateRemoteId(remoteId = remoteId, id = localId) }
                }
            } catch (_: Exception) {}
        }
    }

    private suspend fun syncWithRemote(userId: String) = syncMutex.withLock {
        try {
            // Push unsynced items (no remoteId yet). Items are never edited, so there's no dirty
            // set to reconcile — pushing new rows and pulling remote ones is the whole sync.
            val unsynced = withContext(ioContext) { db.getUnsynced().executeAsList() }
            for (item in unsynced) {
                try {
                    val clientId =
                        item.clientId
                            ?: unique.generate().also { newId ->
                                withContext(ioContext) {
                                    db.updateClientId(clientId = newId, id = item.id)
                                }
                            }
                    val remote =
                        remoteDataSource.upsertItem(
                            RemoteGroceryAutocompleteItem(
                                ownerId = userId,
                                name = item.name,
                                clientId = clientId,
                            )
                        )
                    remote.id?.let { remoteId ->
                        withContext(ioContext) {
                            db.updateRemoteId(remoteId = remoteId, id = item.id)
                        }
                    }
                } catch (_: Exception) {}
            }

            // Pull remote items not yet local.
            val remoteItems = remoteDataSource.fetchAllItems(userId)
            withContext(ioContext) {
                for (remote in remoteItems) {
                    val remoteId = remote.id ?: continue
                    if (db.getByRemoteId(remoteId).executeAsOneOrNull() != null) continue
                    // Adopt the remoteId onto an existing local row matched by clientId, or — since
                    // names are locally unique (COLLATE NOCASE) — by name, so a create from another
                    // device doesn't collide with the unique-name index.
                    val matched =
                        remote.clientId?.let { db.getByClientId(it).executeAsOneOrNull() }
                            ?: db.getByName(remote.name).executeAsOneOrNull()
                    if (matched != null) {
                        db.updateRemoteId(remoteId = remoteId, id = matched.id)
                    } else {
                        db.createWithRemoteId(
                            name = remote.name,
                            remoteId = remoteId,
                            clientId = remote.clientId,
                            ownerId = userId,
                        )
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun AuthState.userIdOrNull(): String? = (this as? AuthState.Authenticated)?.user?.userId

    private fun DbItem.toItem(): GroceryAutocompleteItem =
        GroceryAutocompleteItem(id = id, name = name)
}
