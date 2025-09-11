package com.plusmobileapps.chefmate.grocery.data

data class GroceryItem(
    val id: Long,
    val name: String,
    val isChecked: Boolean = false,
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
