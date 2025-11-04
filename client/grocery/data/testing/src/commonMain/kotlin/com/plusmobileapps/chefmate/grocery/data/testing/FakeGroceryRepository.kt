package com.plusmobileapps.chefmate.grocery.data.testing

import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeGroceryRepository : GroceryRepository {
    private val _groceries = MutableStateFlow<List<GroceryItem>>(emptyList())
    private var nextId = 1L

    override fun getGroceries(): Flow<List<GroceryItem>> = _groceries.asStateFlow()

    override suspend fun addGrocery(name: String) {
        val newItem =
            GroceryItem(
                id = nextId++,
                name = name,
                isChecked = false,
            )
        _groceries.update { it + newItem }
    }

    override suspend fun addGroceries(names: List<String>) {
        val newItems =
            names.map { name ->
                GroceryItem(
                    id = nextId++,
                    name = name,
                    isChecked = false,
                )
            }
        _groceries.update { it + newItems }
    }

    override suspend fun updateChecked(
        item: GroceryItem,
        isChecked: Boolean,
    ) {
        _groceries.update { items ->
            items.map {
                if (it.id == item.id) it.copy(isChecked = isChecked) else it
            }
        }
    }

    override suspend fun deleteGrocery(item: GroceryItem) {
        _groceries.update { items ->
            items.filter { it.id != item.id }
        }
    }

    override suspend fun getGrocery(id: Long): GroceryItem? = _groceries.value.find { it.id == id }

    override suspend fun updateGrocery(item: GroceryItem) {
        _groceries.update { items ->
            items.map {
                if (it.id == item.id) item else it
            }
        }
    }
}
