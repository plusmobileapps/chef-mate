package com.plusmobileapps.chefmate.grocery.data.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.database.Grocery
import com.plusmobileapps.chefmate.database.GroceryListQueries
import com.plusmobileapps.chefmate.database.GroceryQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.grocery.data.IngredientParser
import com.plusmobileapps.chefmate.grocery.data.SyncStatus
import com.plusmobileapps.chefmate.grocery.data.remote.GroceryRemoteDataSource
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryItem
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryList
import com.plusmobileapps.chefmate.util.DateTimeUtil
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.collections.map
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@OptIn(ExperimentalUuidApi::class)
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class GroceryRepositoryImpl(
    private val queries: GroceryQueries,
    private val listQueries: GroceryListQueries,
    @IO private val ioContext: CoroutineContext,
    private val dateTimeUtil: DateTimeUtil,
    private val remoteDataSource: GroceryRemoteDataSource,
    private val authRepository: AuthenticationRepository,
) : GroceryRepository {

    private val scope = CoroutineScope(ioContext + SupervisorJob())
    private val syncMutex = Mutex()
    private val syncingIds = MutableStateFlow<Set<Long>>(emptySet())

    init {
        scope.launch {
            authRepository.state.collect { state ->
                if (state is AuthState.Authenticated) {
                    syncWithRemote(state.user.userId)
                }
            }
        }
    }

    override fun getGroceries(): Flow<List<GroceryItem>> =
        combine(queries.readAll().asFlow().map { it.executeAsList() }, syncingIds) { items, syncing
                ->
                items.map { fromEntity(it, syncing) }
            }
            .flowOn(ioContext)

    override fun getGroceries(listId: Long): Flow<List<GroceryItem>> =
        combine(queries.readByListId(listId).asFlow().map { it.executeAsList() }, syncingIds) {
                items,
                syncing ->
                items.map { fromEntity(it, syncing) }
            }
            .flowOn(ioContext)

    override fun getGroceryLists(): Flow<List<GroceryListModel>> =
        listQueries
            .getAll()
            .asFlow()
            .mapToList(ioContext)
            .map { query ->
                query.map { entity ->
                    GroceryListModel(
                        id = entity.id,
                        name = entity.name,
                        syncStatus =
                            when {
                                entity.isDirty -> SyncStatus.NOT_SYNCED
                                entity.remoteId != null -> SyncStatus.SYNCED
                                else -> SyncStatus.NOT_SYNCED
                            },
                    )
                }
            }
            .flowOn(ioContext)

    override suspend fun addGrocery(name: String) {
        withContext(ioContext) {
            val defaultListId = ensureDefaultList()
            val now = dateTimeUtil.now.toString()
            val clientId = Uuid.random().toString()
            queries.create(
                name = name,
                isChecked = false,
                createdAt = now,
                updatedAt = now,
                clientId = clientId,
                listId = defaultListId,
                recipeName = null,
            )
        }
        pushAddToRemote(name)
    }

    override suspend fun addGrocery(listId: Long, name: String) {
        withContext(ioContext) {
            val now = dateTimeUtil.now.toString()
            val clientId = Uuid.random().toString()
            queries.create(
                name = name,
                isChecked = false,
                createdAt = now,
                updatedAt = now,
                clientId = clientId,
                listId = listId,
                recipeName = null,
            )
        }
        pushAddToRemote(name)
    }

    override suspend fun addGroceries(names: List<String>) {
        withContext(ioContext) {
            val defaultListId = ensureDefaultList()
            val now = dateTimeUtil.now.toString()
            queries.transaction {
                names.forEach { name ->
                    queries.create(
                        name = name,
                        isChecked = false,
                        createdAt = now,
                        updatedAt = now,
                        clientId = Uuid.random().toString(),
                        listId = defaultListId,
                        recipeName = null,
                    )
                }
            }
        }
        names.forEach { pushAddToRemote(it) }
    }

    override suspend fun addGroceries(listId: Long, names: List<String>) {
        withContext(ioContext) {
            val now = dateTimeUtil.now.toString()
            queries.transaction {
                names.forEach { name ->
                    queries.create(
                        name = name,
                        isChecked = false,
                        createdAt = now,
                        updatedAt = now,
                        clientId = Uuid.random().toString(),
                        listId = listId,
                        recipeName = null,
                    )
                }
            }
        }
        names.forEach { pushAddToRemote(it) }
    }

    override suspend fun addGroceries(listId: Long, names: List<String>, recipeName: String?) {
        withContext(ioContext) {
            val now = dateTimeUtil.now.toString()
            queries.transaction {
                names.forEach { name ->
                    queries.create(
                        name = name,
                        isChecked = false,
                        createdAt = now,
                        updatedAt = now,
                        clientId = Uuid.random().toString(),
                        listId = listId,
                        recipeName = recipeName,
                    )
                }
            }
        }
        names.forEach { pushAddToRemote(it) }
    }

    override suspend fun updateChecked(item: GroceryItem, isChecked: Boolean) {
        withContext(ioContext) {
            queries.updateChecked(
                isChecked = isChecked,
                updatedAt = dateTimeUtil.now.toString(),
                id = item.id,
            )
        }
        pushUpdateToRemote(item.id)
    }

    override suspend fun deleteGrocery(item: GroceryItem) {
        val entity =
            withContext(ioContext) {
                val entity = queries.getGroceryById(item.id).executeAsOneOrNull()
                queries.delete(item.id)
                entity
            }
        entity?.remoteId?.let { remoteId ->
            scope.launch {
                try {
                    remoteDataSource.deleteGroceryItem(remoteId)
                } catch (_: Exception) {}
            }
        }
    }

    override suspend fun getGrocery(id: Long): GroceryItem? =
        withContext(ioContext) {
            queries.getGroceryById(id).executeAsOneOrNull()?.let {
                fromEntity(it, syncingIds.value)
            }
        }

    override suspend fun updateGrocery(item: GroceryItem) {
        withContext(ioContext) {
            queries.update(
                name = item.name,
                isChecked = item.isChecked,
                aisle = item.category.name,
                updatedAt = dateTimeUtil.now.toString(),
                id = item.id,
            )
        }
        pushUpdateToRemote(item.id)
    }

    override suspend fun syncAllUnsynced() {
        val authState = authRepository.state.value
        if (authState is AuthState.Authenticated) {
            syncWithRemote(authState.user.userId)
        }
    }

    override suspend fun createGroceryList(name: String): Long =
        withContext(ioContext) {
            val clientId = Uuid.random().toString()
            val id = listQueries.transactionWithResult {
                listQueries.create(name = name, clientId = clientId)
                listQueries.lastId().executeAsOne().MAX!!
            }
            pushListToRemote(id)
            id
        }

    override suspend fun deleteGroceryList(id: Long) {
        val entity =
            withContext(ioContext) {
                val entity = listQueries.getById(id).executeAsOneOrNull()
                listQueries.delete(id)
                entity
            }
        entity?.remoteId?.let { remoteId ->
            scope.launch {
                try {
                    remoteDataSource.deleteGroceryList(remoteId)
                } catch (_: Exception) {}
            }
        }
    }

    override suspend fun renameGroceryList(id: Long, name: String) {
        withContext(ioContext) {
            listQueries.update(name = name, updatedAt = dateTimeUtil.now.toString(), id = id)
        }
        pushListUpdateToRemote(id)
    }

    override suspend fun clearLocalData() {
        withContext(ioContext) {
            queries.deleteAll()
            listQueries.deleteAll()
        }
    }

    override suspend fun ensureDefaultList(): Long =
        withContext(ioContext) {
            val existing = listQueries.getAll().executeAsList()
            if (existing.isNotEmpty()) {
                existing.first().id
            } else {
                listQueries.transactionWithResult {
                    listQueries.create(
                        name = "My Grocery List",
                        clientId = Uuid.random().toString(),
                    )
                    listQueries.lastId().executeAsOne().MAX!!
                }
            }
        }

    private fun pushListToRemote(localId: Long) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        scope.launch {
            try {
                val entity = listQueries.getById(localId).executeAsOneOrNull() ?: return@launch
                val remoteList =
                    remoteDataSource.createGroceryList(
                        RemoteGroceryList(name = entity.name, ownerId = authState.user.userId)
                    )
                listQueries.updateRemoteId(remoteId = remoteList.id!!, id = localId)
            } catch (_: Exception) {}
        }
    }

    private fun pushListUpdateToRemote(localId: Long) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        scope.launch {
            try {
                val entity = listQueries.getById(localId).executeAsOneOrNull() ?: return@launch
                val remoteId = entity.remoteId ?: return@launch
                remoteDataSource.updateGroceryList(
                    RemoteGroceryList(
                        id = remoteId,
                        name = entity.name,
                        ownerId = authState.user.userId,
                    )
                )
                listQueries.clearDirty(localId)
            } catch (_: Exception) {}
        }
    }

    private fun pushAddToRemote(name: String) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        scope.launch {
            try {
                val unsyncedItems = queries.getUnsynced().executeAsList()
                val match = unsyncedItems.firstOrNull { it.name == name }
                if (match != null) {
                    val clientId =
                        match.clientId
                            ?: Uuid.random().toString().also { newId ->
                                queries.updateClientId(clientId = newId, id = match.id)
                            }
                    // Resolve the list's remoteId
                    val listRemoteId =
                        match.listId?.let { localListId ->
                            listQueries.getById(localListId).executeAsOneOrNull()?.remoteId
                        } ?: remoteDataSource.ensureDefaultList(authState.user.userId)

                    syncingIds.update { it + match.id }
                    try {
                        val remoteItem =
                            remoteDataSource.upsertGroceryItem(
                                RemoteGroceryItem(
                                    listId = listRemoteId,
                                    name = match.name,
                                    isChecked = match.isChecked,
                                    createdAt = match.createdAt,
                                    updatedAt = match.updatedAt,
                                    clientId = clientId,
                                    recipeName = match.recipeName,
                                    aisle = match.aisle,
                                )
                            )
                        queries.updateRemoteId(
                            remoteId = remoteItem.id,
                            listRemoteId = listRemoteId,
                            id = match.id,
                        )
                    } finally {
                        syncingIds.update { it - match.id }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun pushUpdateToRemote(localId: Long) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        scope.launch {
            try {
                val entity = queries.getGroceryById(localId).executeAsOneOrNull() ?: return@launch
                val remoteId = entity.remoteId ?: return@launch
                val listId = entity.listRemoteId ?: return@launch
                syncingIds.update { it + localId }
                try {
                    remoteDataSource.upsertGroceryItem(
                        RemoteGroceryItem(
                            id = remoteId,
                            listId = listId,
                            name = entity.name,
                            isChecked = entity.isChecked,
                            updatedAt = entity.updatedAt,
                            clientId = entity.clientId,
                            recipeName = entity.recipeName,
                            aisle = entity.aisle,
                        )
                    )
                    queries.clearDirty(localId)
                } finally {
                    syncingIds.update { it - localId }
                }
            } catch (_: Exception) {}
        }
    }

    private suspend fun syncWithRemote(userId: String) = syncMutex.withLock {
        try {
            // --- Sync lists first ---

            // Pull remote lists BEFORE pushing to avoid creating duplicates.
            // When signing in on a new device, a local default list may already
            // exist. Pulling first lets us link it to the remote list instead of
            // pushing a duplicate.
            val remoteLists = remoteDataSource.fetchGroceryLists(userId)
            val remoteListIds = remoteLists.mapNotNull { it.id }.toSet()
            withContext(ioContext) {
                for (remoteList in remoteLists) {
                    val remoteId = remoteList.id ?: continue
                    val existing = listQueries.getByRemoteId(remoteId).executeAsOneOrNull()
                    if (existing != null) continue

                    // Try to match a local unsynced list by name
                    val localLists = listQueries.getAll().executeAsList()
                    val matchedByName = localLists.firstOrNull {
                        it.remoteId == null && it.name == remoteList.name
                    }
                    if (matchedByName != null) {
                        // Link the local list to the remote one so we don't
                        // create a duplicate. Any local items will be synced
                        // to the remote list below.
                        listQueries.updateRemoteId(remoteId = remoteId, id = matchedByName.id)
                    } else {
                        val newId = listQueries.transactionWithResult {
                            listQueries.create(name = remoteList.name, clientId = null)
                            listQueries.lastId().executeAsOne().MAX!!
                        }
                        listQueries.updateRemoteId(remoteId = remoteId, id = newId)
                    }
                }

                // Prune local lists that were deleted on another device.
                // A local list with a remoteId that no longer appears in the
                // fetched remote lists has been deleted remotely — drop it and
                // its items locally so the deletion is reflected on this device.
                val pruneLists =
                    listQueries.getAll().executeAsList().filter {
                        it.remoteId != null && it.remoteId !in remoteListIds
                    }
                for (list in pruneLists) {
                    queries.deleteByListId(list.id)
                    listQueries.delete(list.id)
                }
            }

            // Push unsynced lists (only those that weren't linked to a remote list above)
            val unsyncedLists = withContext(ioContext) { listQueries.getUnsynced().executeAsList() }
            for (list in unsyncedLists) {
                try {
                    val remoteList =
                        remoteDataSource.createGroceryList(
                            RemoteGroceryList(name = list.name, ownerId = userId)
                        )
                    withContext(ioContext) {
                        listQueries.updateRemoteId(remoteId = remoteList.id!!, id = list.id)
                    }
                } catch (_: Exception) {}
            }

            // Push dirty lists
            val dirtyLists = withContext(ioContext) { listQueries.getDirty().executeAsList() }
            for (list in dirtyLists) {
                try {
                    val remoteId = list.remoteId ?: continue
                    remoteDataSource.updateGroceryList(
                        RemoteGroceryList(id = remoteId, name = list.name, ownerId = userId)
                    )
                    withContext(ioContext) { listQueries.clearDirty(list.id) }
                } catch (_: Exception) {}
            }

            // --- Sync items per list ---
            val allLists = withContext(ioContext) { listQueries.getAll().executeAsList() }

            for (list in allLists) {
                val listRemoteId = list.remoteId ?: continue

                // Push unsynced items for this list
                val unsynced =
                    withContext(ioContext) {
                        queries.getUnsynced().executeAsList().filter { it.listId == list.id }
                    }
                for (item in unsynced) {
                    try {
                        val clientId =
                            item.clientId
                                ?: Uuid.random().toString().also { newId ->
                                    withContext(ioContext) {
                                        queries.updateClientId(clientId = newId, id = item.id)
                                    }
                                }
                        syncingIds.update { it + item.id }
                        try {
                            val remoteItem =
                                remoteDataSource.upsertGroceryItem(
                                    RemoteGroceryItem(
                                        listId = listRemoteId,
                                        name = item.name,
                                        isChecked = item.isChecked,
                                        createdAt = item.createdAt,
                                        updatedAt = item.updatedAt,
                                        clientId = clientId,
                                        recipeName = item.recipeName,
                                        aisle = item.aisle,
                                    )
                                )
                            withContext(ioContext) {
                                queries.updateRemoteId(
                                    remoteId = remoteItem.id,
                                    listRemoteId = listRemoteId,
                                    id = item.id,
                                )
                            }
                        } finally {
                            syncingIds.update { it - item.id }
                        }
                    } catch (_: Exception) {}
                }

                // Push dirty items for this list
                val dirty =
                    withContext(ioContext) {
                        queries.getDirty().executeAsList().filter { it.listId == list.id }
                    }
                for (item in dirty) {
                    try {
                        val remoteId = item.remoteId ?: continue
                        val itemListId = item.listRemoteId ?: listRemoteId
                        syncingIds.update { it + item.id }
                        try {
                            remoteDataSource.upsertGroceryItem(
                                RemoteGroceryItem(
                                    id = remoteId,
                                    listId = itemListId,
                                    name = item.name,
                                    isChecked = item.isChecked,
                                    updatedAt = item.updatedAt,
                                    clientId = item.clientId,
                                    recipeName = item.recipeName,
                                    aisle = item.aisle,
                                )
                            )
                            withContext(ioContext) { queries.clearDirty(item.id) }
                        } finally {
                            syncingIds.update { it - item.id }
                        }
                    } catch (_: Exception) {}
                }

                // Pull remote items for this list
                val remoteItems = remoteDataSource.fetchAllGroceryItems(listRemoteId)
                val remoteItemIds = remoteItems.mapNotNull { it.id }.toSet()
                withContext(ioContext) {
                    for (remoteItem in remoteItems) {
                        val remoteId = remoteItem.id ?: continue
                        val existing = queries.getByRemoteId(remoteId).executeAsOneOrNull()
                        if (existing != null) continue

                        val matchedByClientId =
                            remoteItem.clientId?.let { clientId ->
                                queries.getByClientId(clientId).executeAsOneOrNull()
                            }
                        if (matchedByClientId != null) {
                            queries.updateRemoteId(
                                remoteId = remoteId,
                                listRemoteId = listRemoteId,
                                id = matchedByClientId.id,
                            )
                        } else {
                            queries.createWithRemoteId(
                                name = remoteItem.name,
                                isChecked = remoteItem.isChecked,
                                createdAt = remoteItem.createdAt ?: dateTimeUtil.now.toString(),
                                updatedAt = remoteItem.updatedAt ?: dateTimeUtil.now.toString(),
                                remoteId = remoteId,
                                listRemoteId = listRemoteId,
                                clientId = remoteItem.clientId,
                                listId = list.id,
                                recipeName = remoteItem.recipeName,
                                aisle = remoteItem.aisle,
                            )
                        }
                    }

                    // Prune local items that were deleted on another device.
                    // An item with a remoteId that no longer appears in the
                    // fetched remote items has been deleted remotely. Items
                    // without a remoteId (still pending initial push) are
                    // preserved.
                    val pruneItems =
                        queries.readByListId(list.id).executeAsList().filter {
                            it.remoteId != null && it.remoteId !in remoteItemIds
                        }
                    for (item in pruneItems) {
                        queries.delete(item.id)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    override suspend fun deleteAllGroceries(listId: Long) {
        val remoteIds =
            withContext(ioContext) {
                val items = queries.readByListId(listId).executeAsList()
                queries.deleteByListId(listId)
                items.mapNotNull { it.remoteId }
            }
        deleteRemoteItems(remoteIds)
    }

    override suspend fun deletePurchasedGroceries(listId: Long) {
        val remoteIds =
            withContext(ioContext) {
                val items = queries.readCheckedByListId(listId).executeAsList()
                queries.deleteCheckedByListId(listId)
                items.mapNotNull { it.remoteId }
            }
        deleteRemoteItems(remoteIds)
    }

    private fun deleteRemoteItems(remoteIds: List<String>) {
        if (remoteIds.isEmpty()) return
        scope.launch {
            for (remoteId in remoteIds) {
                try {
                    remoteDataSource.deleteGroceryItem(remoteId)
                } catch (_: Exception) {}
            }
        }
    }

    private fun fromEntity(entity: Grocery, syncing: Set<Long>): GroceryItem {
        val syncStatus =
            when {
                entity.id in syncing -> SyncStatus.SYNCING
                entity.isDirty -> SyncStatus.NOT_SYNCED
                entity.remoteId != null -> SyncStatus.SYNCED
                else -> SyncStatus.NOT_SYNCED
            }
        val parsed = IngredientParser.parse(entity.name)
        val storedAisle =
            entity.aisle?.let { runCatching { GroceryCategory.valueOf(it) }.getOrNull() }
        return GroceryItem(
            id = entity.id,
            name = entity.name,
            displayName = parsed.name,
            quantity = parsed.quantity,
            category = storedAisle ?: parsed.category,
            isChecked = entity.isChecked,
            syncStatus = syncStatus,
            recipeName = entity.recipeName,
        )
    }
}
