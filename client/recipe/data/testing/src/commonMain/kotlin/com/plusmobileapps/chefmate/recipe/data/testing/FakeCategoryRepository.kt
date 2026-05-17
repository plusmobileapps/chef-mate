package com.plusmobileapps.chefmate.recipe.data.testing

import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCategoryRepository(
    private val categories: MutableStateFlow<List<Category>> = MutableStateFlow(emptyList())
) : CategoryRepository {

    // Tests are single-threaded; non-atomic counter is fine.
    private var nextId: Long = 1L

    private fun newId(): Long = nextId++

    override fun observeUserCategories(): Flow<List<Category>> = categories.asStateFlow()

    override suspend fun findBuiltin(builtin: BuiltinCategory): Category? =
        categories.value.firstOrNull { it.builtinId == builtin.id }

    override suspend fun materializeBuiltin(builtin: BuiltinCategory): Category =
        findBuiltin(builtin)
            ?: Category(id = newId(), name = builtin.id, builtinId = builtin.id).also { created ->
                categories.value = categories.value + created
            }

    override suspend fun createUserCategory(name: String): Category =
        Category(id = newId(), name = name).also { created ->
            categories.value = categories.value + created
        }

    override suspend fun renameCategory(id: Long, name: String): Category {
        val updated = categories.value.map { if (it.id == id) it.copy(name = name) else it }
        categories.value = updated
        return updated.first { it.id == id }
    }

    override suspend fun deleteCategory(id: Long) {
        categories.value = categories.value.filterNot { it.id == id }
    }

    override suspend fun clearLocalData() {
        categories.value = emptyList()
    }
}
