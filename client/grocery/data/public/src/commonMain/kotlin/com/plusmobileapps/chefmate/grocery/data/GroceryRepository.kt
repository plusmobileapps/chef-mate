@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.grocery.data

import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow

interface GroceryRepository {
    fun getGroceries(): Flow<List<GroceryItem>>

    fun getGroceries(listId: Long): Flow<List<GroceryItem>>

    fun getGroceryLists(): Flow<List<GroceryListModel>>

    suspend fun addGrocery(name: String)

    suspend fun addGrocery(listId: Long, name: String)

    suspend fun addGroceries(names: List<String>)

    suspend fun addGroceries(listId: Long, names: List<String>)

    suspend fun addGroceries(listId: Long, names: List<String>, recipeName: String?)

    suspend fun updateChecked(item: GroceryItem, isChecked: Boolean)

    suspend fun deleteGrocery(item: GroceryItem)

    suspend fun getGrocery(id: Long): GroceryItem?

    suspend fun updateGrocery(item: GroceryItem)

    suspend fun syncAllUnsynced()

    suspend fun createGroceryList(name: String): Long

    suspend fun deleteGroceryList(id: Long)

    suspend fun renameGroceryList(id: Long, name: String)

    suspend fun ensureDefaultList(): Long

    suspend fun deleteAllGroceries(listId: Long)

    suspend fun deletePurchasedGroceries(listId: Long)

    suspend fun clearLocalData()
}
