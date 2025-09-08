package com.plusmobileapps.chefmate.grocerylist

data class GroceryItem(
    val id: Long,
    val name: String,
    val isChecked: Boolean = false
)