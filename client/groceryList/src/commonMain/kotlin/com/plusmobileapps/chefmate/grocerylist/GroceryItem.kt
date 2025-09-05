package com.plusmobileapps.chefmate.grocerylist

import com.plusmobileapps.chefmate.database.Grocery

data class GroceryItem(
    val id: Long,
    val name: String,
    val isChecked: Boolean = false
) {
    companion object {
        fun fromEntity(entity: Grocery): GroceryItem {
            return GroceryItem(
                id = entity.id,
                name = entity.name,
                isChecked = entity.isChecked
            )
        }
    }
}