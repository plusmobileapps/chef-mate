@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.grocery

import app.cash.sqldelight.coroutines.asFlow
import com.plusmobileapps.chefmate.database.Grocery
import com.plusmobileapps.chefmate.database.GroceryQueries
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(
    scope = AppScope::class,
    boundType = GroceryRepository::class,
)
class GroceryRepositoryImpl(
    private val queries: GroceryQueries,
    @IO private val ioContext: CoroutineContext,
) : GroceryRepository {
    override fun getGroceries(): Flow<List<GroceryItem>> =
        queries
            .readAll()
            .asFlow()
            .map { it.executeAsList() }
            .map { items -> items.map { fromEntity(it) } }
            .flowOn(ioContext)

    override suspend fun addGrocery(name: String) {
        withContext(ioContext) {
            val now = Clock.System.now().toString()
            queries.create(name = name, isChecked = false, createdAt = now, updatedAt = now)
        }
    }

    override suspend fun updateChecked(
        item: GroceryItem,
        isChecked: Boolean,
    ) {
        withContext(ioContext) {
            queries.updateChecked(
                isChecked = isChecked,
                updatedAt = Clock.System.now().toString(),
                id = item.id,
            )
        }
    }

    override suspend fun deleteGrocery(item: GroceryItem) {
        withContext(ioContext) {
            queries.delete(item.id)
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
                updatedAt = Clock.System.now().toString(),
                id = item.id,
            )
        }
    }

    fun fromEntity(entity: Grocery): GroceryItem =
        GroceryItem(
            id = entity.id,
            name = entity.name,
            isChecked = entity.isChecked,
        )
}
