package com.plusmobileapps.chefmate.grocery.data.testing

import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeGroceryRepository : GroceryRepository {
    private val _groceries = MutableStateFlow<List<GroceryItem>>(emptyList())
    private val _lists = MutableStateFlow(
        listOf(GroceryListModel(id = 1L, name = "My Grocery List")),
    )
    private var nextId = 1L
    private var nextListId = 2L
    private val itemListMap = mutableMapOf<Long, Long>()

    override fun getGroceries(): Flow<List<GroceryItem>> = _groceries.asStateFlow()

    override fun getGroceries(listId: Long): Flow<List<GroceryItem>> =
        _groceries.map { items ->
            items.filter { itemListMap[it.id] == listId }
        }

    override fun getGroceryLists(): Flow<List<GroceryListModel>> = _lists.asStateFlow()

    override suspend fun addGrocery(name: String) {
        val defaultListId = _lists.value.firstOrNull()?.id ?: 1L
        val newItem = GroceryItem(id = nextId++, name = name, isChecked = false)
        itemListMap[newItem.id] = defaultListId
        _groceries.update { it + newItem }
    }

    override suspend fun addGrocery(listId: Long, name: String) {
        val newItem = GroceryItem(id = nextId++, name = name, isChecked = false)
        itemListMap[newItem.id] = listId
        _groceries.update { it + newItem }
    }

    override suspend fun addGroceries(names: List<String>) {
        val defaultListId = _lists.value.firstOrNull()?.id ?: 1L
        val newItems = names.map { name ->
            val item = GroceryItem(id = nextId++, name = name, isChecked = false)
            itemListMap[item.id] = defaultListId
            item
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
        itemListMap.remove(item.id)
        _groceries.update { items ->
            items.filter { it.id != item.id }
        }
    }

    override suspend fun getGrocery(id: Long): GroceryItem? = _groceries.value.find { it.id == id }

    override suspend fun syncAllUnsynced() {}

    override suspend fun updateGrocery(item: GroceryItem) {
        _groceries.update { items ->
            items.map {
                if (it.id == item.id) item else it
            }
        }
    }

    override suspend fun createGroceryList(name: String): Long {
        val id = nextListId++
        _lists.update { it + GroceryListModel(id = id, name = name) }
        return id
    }

    override suspend fun deleteGroceryList(id: Long) {
        _lists.update { lists -> lists.filter { it.id != id } }
        val itemIds = itemListMap.filter { it.value == id }.keys
        itemListMap.keys.removeAll(itemIds)
        _groceries.update { items -> items.filter { it.id !in itemIds } }
    }

    override suspend fun renameGroceryList(id: Long, name: String) {
        _lists.update { lists ->
            lists.map { if (it.id == id) it.copy(name = name) else it }
        }
    }

    override suspend fun ensureDefaultList(): Long {
        val existing = _lists.value.firstOrNull()
        if (existing != null) return existing.id
        val id = nextListId++
        _lists.update { it + GroceryListModel(id = id, name = "My Grocery List") }
        return id
    }
}
