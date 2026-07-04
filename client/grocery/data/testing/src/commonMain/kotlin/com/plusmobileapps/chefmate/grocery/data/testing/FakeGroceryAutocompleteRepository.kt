package com.plusmobileapps.chefmate.grocery.data.testing

import com.plusmobileapps.chefmate.grocery.data.GroceryAutocompleteItem
import com.plusmobileapps.chefmate.grocery.data.GroceryAutocompleteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeGroceryAutocompleteRepository(
    private val items: MutableStateFlow<List<GroceryAutocompleteItem>> =
        MutableStateFlow(emptyList())
) : GroceryAutocompleteRepository {

    // Tests are single-threaded; non-atomic counter is fine.
    private var nextId: Long = 1L

    override fun observeItems(): Flow<List<GroceryAutocompleteItem>> = items.asStateFlow()

    override suspend fun addItem(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        if (items.value.any { it.name.equals(trimmed, ignoreCase = true) }) return
        items.value =
            (items.value + GroceryAutocompleteItem(id = nextId++, name = trimmed)).sortedBy {
                it.name.lowercase()
            }
    }

    override suspend fun deleteItem(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun clearLocalData() {
        items.value = emptyList()
    }
}
