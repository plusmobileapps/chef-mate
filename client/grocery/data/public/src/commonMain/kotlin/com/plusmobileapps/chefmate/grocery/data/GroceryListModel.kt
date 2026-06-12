package com.plusmobileapps.chefmate.grocery.data

data class GroceryListModel(
    val id: Long,
    val name: String,
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
    val role: ListRole = ListRole.OWNER,
    val isShared: Boolean = false,
)
