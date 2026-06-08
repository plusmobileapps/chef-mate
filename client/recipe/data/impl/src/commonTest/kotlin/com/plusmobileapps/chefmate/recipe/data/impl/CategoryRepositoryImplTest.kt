@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)

package com.plusmobileapps.chefmate.recipe.data.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.testing.createTestDatabase
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.impl.remote.CategoryRemoteDataSource
import com.plusmobileapps.chefmate.recipe.data.impl.remote.RemoteCategory
import com.plusmobileapps.chefmate.util.testing.FakeUnique
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class CategoryRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val db: Database = createTestDatabase()
    private val fakeAuth = FakeAuthenticationRepository()
    private val fakeRemote = RecordingRemote()

    private val repository =
        CategoryRepositoryImpl(
            db = db.categoryQueries,
            recipeCategoryQueries = db.recipeCategoryQueries,
            ioContext = testDispatcher,
            unique = FakeUnique(),
            remoteDataSource = fakeRemote,
            authRepository = fakeAuth,
        )

    @Test
    fun materializeBuiltin_inserts_a_row_the_first_time() =
        runTest(testDispatcher) {
            val materialized = repository.materializeBuiltin(BuiltinCategory.BREAKFAST)
            materialized.builtinId shouldBe BuiltinCategory.BREAKFAST.id

            repository.observeUserCategories().test {
                val items = awaitItem()
                items.size shouldBe 1
                items.first().builtinId shouldBe BuiltinCategory.BREAKFAST.id
            }
        }

    @Test
    fun materializeBuiltin_returns_the_same_row_on_subsequent_calls() =
        runTest(testDispatcher) {
            // Two materializations of the same preset must return the same DB row — otherwise the
            // partial unique index would reject the second insert and we'd get inconsistent state.
            val first = repository.materializeBuiltin(BuiltinCategory.LUNCH)
            val second = repository.materializeBuiltin(BuiltinCategory.LUNCH)
            first.id shouldBe second.id

            repository.observeUserCategories().test { awaitItem().size shouldBe 1 }
        }

    @Test
    fun createUserCategory_inserts_a_row_with_no_builtinId() =
        runTest(testDispatcher) {
            val created = repository.createUserCategory("Weeknight Quick")
            created.name shouldBe "Weeknight Quick"
            created.builtinId shouldBe null
        }

    @Test
    fun deleteCategory_removes_it_from_observed_list() =
        runTest(testDispatcher) {
            val created = repository.createUserCategory("Brunch")
            repository.deleteCategory(created.id)

            repository.observeUserCategories().test { awaitItem() shouldBe emptyList() }
        }

    private class RecordingRemote : CategoryRemoteDataSource {
        var lastUpsert: RemoteCategory? = null
            private set

        override suspend fun upsertCategory(category: RemoteCategory): RemoteCategory =
            category.copy(id = category.id ?: "remote-${category.clientId}").also {
                lastUpsert = it
            }

        override suspend fun deleteCategory(remoteId: String) = Unit

        override suspend fun fetchAllCategories(ownerId: String): List<RemoteCategory> = emptyList()
    }
}
