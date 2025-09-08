package com.plusmobileapps.chefmate.grocerylist

import com.plusmobileapps.chefmate.database.Grocery

data class GroceryItem(
    val id: Long,
    val name: String,
    val isChecked: Boolean = false
) {
    companion object {
        val empty = GroceryItem(
            id = 0L,
            name = "",
            isChecked = false
        )

        fun fromEntity(entity: Grocery): GroceryItem {
            return GroceryItem(
                id = entity.id,
                name = entity.name,
                isChecked = entity.isChecked
            )
        }
    }
}