@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.recipebook.data.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.testing.createTestDatabase
import com.plusmobileapps.chefmate.recipebook.data.impl.remote.RecipeBookRemoteDataSource
import com.plusmobileapps.chefmate.recipebook.data.impl.remote.RemoteRecipeBook
import com.plusmobileapps.chefmate.util.testing.FakeDateTimeUtil
import com.russhwolf.settings.MapSettings
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class RecipeBookRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val db: Database = createTestDatabase()
    private val fakeAuth = FakeAuthenticationRepository()
    private val dateTimeUtil = FakeDateTimeUtil()
    private val settings = MapSettings()

    private fun repository() =
        RecipeBookRepositoryImpl(
            db = db.recipeBookQueries,
            ioContext = testDispatcher,
            dateTimeUtil = dateTimeUtil,
            remoteDataSource = NoopRecipeBookRemote(),
            authRepository = fakeAuth,
            settings = settings,
        )

    @Test
    fun ensures_exactly_one_default_book_on_first_use() =
        runTest(testDispatcher) {
            val repo = repository()

            repo.getRecipeBooks().test {
                val books = awaitItem()
                books.size shouldBe 1
                books.single().isDefault shouldBe true
                books.single().name shouldBe "My Recipes"
            }
        }

    @Test
    fun activeBookId_resolves_to_the_default_book_initially() =
        runTest(testDispatcher) {
            val repo = repository()
            val defaultId = repo.getDefaultBookId()

            repo.activeBookId.value shouldBe defaultId
        }

    @Test
    fun createBook_adds_a_non_default_book() =
        runTest(testDispatcher) {
            val repo = repository()

            val created = repo.createBook("Weeknight Dinners")

            created.name shouldBe "Weeknight Dinners"
            created.isDefault shouldBe false
            repo.getRecipeBooks().test { awaitItem().size shouldBe 2 }
        }

    @Test
    fun renameBook_changes_the_name() =
        runTest(testDispatcher) {
            val repo = repository()
            val created = repo.createBook("Old name")

            repo.renameBook(created.id, "New name")

            repo.getRecipeBook(created.id).test { awaitItem()?.name shouldBe "New name" }
        }

    @Test
    fun setActiveBook_updates_active_id_and_persists() =
        runTest(testDispatcher) {
            val repo = repository()
            val created = repo.createBook("Holiday Baking")

            repo.setActiveBook(created.id)

            repo.activeBookId.value shouldBe created.id
            // A fresh repository reading the same settings restores the selection.
            repository().activeBookId.value shouldBe created.id
        }

    private class NoopRecipeBookRemote : RecipeBookRemoteDataSource {
        override suspend fun upsertRecipeBook(book: RemoteRecipeBook): RemoteRecipeBook = book

        override suspend fun deleteRecipeBook(remoteId: String) = Unit

        override suspend fun fetchAllRecipeBooks(ownerId: String): List<RemoteRecipeBook> =
            emptyList()
    }
}
