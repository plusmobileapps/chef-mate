package com.plusmobileapps.chefmate.grocery.data.impl

import app.cash.sqldelight.coroutines.asFlow
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.database.Grocery
import com.plusmobileapps.chefmate.database.GroceryQueries
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.grocery.data.impl.remote.GroceryRemoteDataSource
import com.plusmobileapps.chefmate.grocery.data.impl.remote.RemoteGroceryItem
import com.plusmobileapps.chefmate.util.DateTimeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.collections.map
import kotlin.coroutines.CoroutineContext

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(
    scope = AppScope::class,
    boundType = GroceryRepository::class,
)
class GroceryRepositoryImpl(
    private val queries: GroceryQueries,
    @IO private val ioContext: CoroutineContext,
    private val dateTimeUtil: DateTimeUtil,
    private val remoteDataSource: GroceryRemoteDataSource,
    private val authRepository: AuthenticationRepository,
) : GroceryRepository {

    private val scope = CoroutineScope(ioContext + SupervisorJob())

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
        queries
            .readAll()
            .asFlow()
            .map { it.executeAsList() }
            .map { items -> items.map { fromEntity(it) } }
            .flowOn(ioContext)

    override suspend fun addGrocery(name: String) {
        withContext(ioContext) {
            val now = dateTimeUtil.now.toString()
            queries.create(name = name, isChecked = false, createdAt = now, updatedAt = now)
        }
        pushAddToRemote(name)
    }

    override suspend fun addGroceries(names: List<String>) {
        withContext(ioContext) {
            val now = dateTimeUtil.now.toString()
            queries.transaction {
                names.forEach { name ->
                    queries.create(
                        name = name,
                        isChecked = false,
                        createdAt = now,
                        updatedAt = now,
                    )
                }
            }
        }
        names.forEach { pushAddToRemote(it) }
    }

    override suspend fun updateChecked(
        item: GroceryItem,
        isChecked: Boolean,
    ) {
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
        val entity = withContext(ioContext) {
            val entity = queries.getGroceryById(item.id).executeAsOneOrNull()
            queries.delete(item.id)
            entity
        }
        entity?.remoteId?.let { remoteId ->
            scope.launch {
                try {
                    remoteDataSource.deleteGroceryItem(remoteId)
                } catch (_: Exception) {
                }
            }
        }
    }

    override suspend fun getGrocery(id: Long): GroceryItem? =
        withContext(ioContext) {
            queries
                .getGroceryById(id)
                .executeAsOneOrNull()
                ?.let { fromEntity(it) }
        }

    override suspend fun updateGrocery(item: GroceryItem) {
        withContext(ioContext) {
            queries.update(
                name = item.name,
                isChecked = item.isChecked,
                updatedAt = dateTimeUtil.now.toString(),
                id = item.id,
            )
        }
        pushUpdateToRemote(item.id)
    }

    private fun pushAddToRemote(name: String) {
        val authState = authRepository.state.value
        if (authState !is AuthState.Authenticated) return
        scope.launch {
            try {
                val listId = remoteDataSource.ensureDefaultList(authState.user.userId)
                val unsyncedItems = queries.getUnsynced().executeAsList()
                val match = unsyncedItems.firstOrNull { it.name == name }
                if (match != null) {
                    val remoteItem = remoteDataSource.upsertGroceryItem(
                        RemoteGroceryItem(
                            listId = listId,
                            name = match.name,
                            isChecked = match.isChecked,
                            createdAt = match.createdAt,
                            updatedAt = match.updatedAt,
                        ),
                    )
                    queries.updateRemoteId(
                        remoteId = remoteItem.id,
                        listRemoteId = listId,
                        id = match.id,
                    )
                }
            } catch (_: Exception) {
            }
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
                remoteDataSource.upsertGroceryItem(
                    RemoteGroceryItem(
                        id = remoteId,
                        listId = listId,
                        name = entity.name,
                        isChecked = entity.isChecked,
                        updatedAt = entity.updatedAt,
                    ),
                )
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun syncWithRemote(userId: String) {
        try {
            val listId = remoteDataSource.ensureDefaultList(userId)

            // Push unsynced items
            val unsynced = withContext(ioContext) {
                queries.getUnsynced().executeAsList()
            }
            for (item in unsynced) {
                try {
                    val remoteItem = remoteDataSource.upsertGroceryItem(
                        RemoteGroceryItem(
                            listId = listId,
                            name = item.name,
                            isChecked = item.isChecked,
                            createdAt = item.createdAt,
                            updatedAt = item.updatedAt,
                        ),
                    )
                    withContext(ioContext) {
                        queries.updateRemoteId(
                            remoteId = remoteItem.id,
                            listRemoteId = listId,
                            id = item.id,
                        )
                    }
                } catch (_: Exception) {
                }
            }

            // Pull remote items
            val remoteItems = remoteDataSource.fetchAllGroceryItems(listId)
            withContext(ioContext) {
                for (remoteItem in remoteItems) {
                    val remoteId = remoteItem.id ?: continue
                    val existing = queries.getByRemoteId(remoteId).executeAsOneOrNull()
                    if (existing == null) {
                        queries.createWithRemoteId(
                            name = remoteItem.name,
                            isChecked = remoteItem.isChecked,
                            createdAt = remoteItem.createdAt ?: dateTimeUtil.now.toString(),
                            updatedAt = remoteItem.updatedAt ?: dateTimeUtil.now.toString(),
                            remoteId = remoteId,
                            listRemoteId = listId,
                        )
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    fun fromEntity(entity: Grocery): GroceryItem =
        GroceryItem(
            id = entity.id,
            name = entity.name,
            isChecked = entity.isChecked,
        )
}
