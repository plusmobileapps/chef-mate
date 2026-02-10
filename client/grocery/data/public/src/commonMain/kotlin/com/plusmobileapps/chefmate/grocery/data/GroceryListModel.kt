package com.plusmobileapps.chefmate.grocery.data

data class GroceryListModel(
    val id: Long,
    val name: String,
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
)
