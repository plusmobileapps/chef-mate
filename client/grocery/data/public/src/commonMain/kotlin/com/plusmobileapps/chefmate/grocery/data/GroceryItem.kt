package com.plusmobileapps.chefmate.grocery.data

data class GroceryItem(
    val id: Long,
    val name: String,
    val isChecked: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
) {
    companion object {
        val empty =
            GroceryItem(
                id = 0L,
                name = "",
                isChecked = false,
            )
    }
}

enum class SyncStatus {
    NOT_SYNCED,
    SYNCING,
    SYNCED,
}
