package com.plusmobileapps.chefmate.grocery.data.impl

import app.cash.sqldelight.coroutines.asFlow
import com.plusmobileapps.chefmate.database.GroceryAutocompleteItem as DbItem
import com.plusmobileapps.chefmate.database.GroceryAutocompleteItemQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.grocery.data.GroceryAutocompleteItem
import com.plusmobileapps.chefmate.grocery.data.GroceryAutocompleteRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@SingleIn(AppScope::class)
@Inject
@ContributesBinding(AppScope::class)
class GroceryAutocompleteRepositoryImpl(
    private val db: GroceryAutocompleteItemQueries,
    @IO private val ioContext: CoroutineContext,
) : GroceryAutocompleteRepository {

    override fun observeItems(): Flow<List<GroceryAutocompleteItem>> =
        db.getAll()
            .asFlow()
            .map { it.executeAsList().map { row -> row.toItem() } }
            .flowOn(ioContext)

    override suspend fun addItem(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        // ownerId stays null while local-only; the sync follow-up populates it. The unique
        // COLLATE NOCASE index + INSERT OR IGNORE dedups case-insensitively.
        withContext(ioContext) { db.insertOrIgnore(name = trimmed, ownerId = null) }
    }

    override suspend fun deleteItem(id: Long) {
        withContext(ioContext) { db.deleteById(id) }
    }

    override suspend fun clearLocalData() {
        withContext(ioContext) { db.deleteAll() }
    }

    private fun DbItem.toItem(): GroceryAutocompleteItem =
        GroceryAutocompleteItem(id = id, name = name)
}
