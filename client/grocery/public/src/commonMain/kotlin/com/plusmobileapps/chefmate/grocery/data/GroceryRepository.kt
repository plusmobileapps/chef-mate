@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.grocery.data

import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime

interface GroceryRepository {
    fun getGroceries(): Flow<List<GroceryItem>>

    suspend fun addGrocery(name: String)

    suspend fun updateChecked(
        item: GroceryItem,
        isChecked: Boolean,
    )

    suspend fun deleteGrocery(item: GroceryItem)

    suspend fun getGrocery(id: Long): GroceryItem?

    suspend fun updateGrocery(item: GroceryItem)
}
