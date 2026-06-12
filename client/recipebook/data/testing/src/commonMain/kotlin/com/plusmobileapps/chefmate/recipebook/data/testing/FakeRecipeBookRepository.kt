@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipebook.data.testing

import com.plusmobileapps.chefmate.recipebook.data.RecipeBook
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRepository
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeRecipeBookRepository(
    private val books: MutableStateFlow<List<RecipeBook>> =
        MutableStateFlow(listOf(RecipeBook.Sample))
) : RecipeBookRepository {

    private val _activeBookId = MutableStateFlow<Long?>(books.value.firstOrNull()?.id)
    override val activeBookId: StateFlow<Long?> = _activeBookId.asStateFlow()

    private var nextId: Long = (books.value.maxOfOrNull { it.id } ?: 0L) + 1

    override fun getRecipeBooks(): Flow<List<RecipeBook>> = books.asStateFlow()

    override fun getRecipeBook(id: Long): Flow<RecipeBook?> = books.map { list ->
        list.firstOrNull { it.id == id }
    }

    override suspend fun getDefaultBookId(): Long =
        books.value.firstOrNull { it.isDefault }?.id ?: books.value.first().id

    override suspend fun setActiveBook(id: Long) {
        _activeBookId.value = id
    }

    override suspend fun selectAllRecipes() {
        _activeBookId.value = null
    }

    override suspend fun createBook(name: String): RecipeBook {
        val book =
            RecipeBook(
                id = nextId++,
                name = name,
                isDefault = false,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        books.value = books.value + book
        return book
    }

    override suspend fun renameBook(id: Long, name: String): RecipeBook {
        books.value = books.value.map { if (it.id == id) it.copy(name = name) else it }
        return books.value.first { it.id == id }
    }

    override suspend fun syncAllUnsynced() {}

    override suspend fun clearLocalData() {
        books.value = emptyList()
    }
}
