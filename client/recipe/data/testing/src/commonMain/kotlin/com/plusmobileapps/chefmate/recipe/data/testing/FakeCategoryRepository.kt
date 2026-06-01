package com.plusmobileapps.chefmate.recipe.data.testing

import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.CategoryRepository
import com.plusmobileapps.chefmate.recipe.data.CategoryWithCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

class FakeCategoryRepository(
    private val categories: MutableStateFlow<List<Category>> = MutableStateFlow(emptyList()),
    /** Maps categoryId → recipeCount; categories missing from the map are reported as 0. */
    private val recipeCounts: MutableStateFlow<Map<Long, Int>> = MutableStateFlow(emptyMap()),
) : CategoryRepository {

    // Tests are single-threaded; non-atomic counter is fine.
    private var nextId: Long = 1L

    private fun newId(): Long = nextId++

    override fun observeUserCategories(): Flow<List<Category>> = categories.asStateFlow()

    override fun observeCategoriesWithCounts(): Flow<List<CategoryWithCount>> =
        categories.combine(recipeCounts) { concrete, counts ->
            val materialized = concrete.mapNotNull { it.builtinId }.toSet()
            val synthetic =
                BuiltinCategory.entries
                    .filter { it.id !in materialized }
                    .map { builtin ->
                        CategoryWithCount(
                            category = Category(id = 0L, name = builtin.id, builtinId = builtin.id),
                            recipeCount = 0,
                        )
                    }
            val concreteWithCounts = concrete.map { CategoryWithCount(it, counts[it.id] ?: 0) }
            (concreteWithCounts + synthetic).sortedBy { it.category.name.lowercase() }
        }

    fun setRecipeCount(categoryId: Long, count: Int) {
        recipeCounts.value = recipeCounts.value + (categoryId to count)
    }

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
