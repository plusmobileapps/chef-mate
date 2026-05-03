@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.browser.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.browser.BROWSER_HISTORY_ENABLED_KEY
import com.plusmobileapps.chefmate.database.testing.createTestDatabase
import com.russhwolf.settings.MapSettings
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class BrowserHistoryRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val db = createTestDatabase()
    private val settings = MapSettings()
    private val repository =
        BrowserHistoryRepositoryImpl(
            queries = db.browserHistoryQueries,
            settings = settings,
            ioContext = testDispatcher,
        )

    @Test
    fun recordVisit_inserts_row() =
        runTest(testDispatcher) {
            repository.recordVisit("https://example.com/recipe")
            repository.observeRecent().test {
                val items = awaitItem()
                items.size shouldBe 1
                items[0].url shouldBe "https://example.com/recipe"
            }
        }

    @Test
    fun recordVisit_is_no_op_when_history_disabled() =
        runTest(testDispatcher) {
            settings.putBoolean(BROWSER_HISTORY_ENABLED_KEY, false)
            repository.recordVisit("https://example.com/recipe")
            repository.observeRecent().test { awaitItem().size shouldBe 0 }
        }

    @Test
    fun recordVisit_dedupes_same_url_and_keeps_one_row() =
        runTest(testDispatcher) {
            repository.recordVisit("https://example.com/recipe")
            repository.recordVisit("https://example.com/recipe")
            repository.observeRecent().test { awaitItem().size shouldBe 1 }
        }

    @Test
    fun recordVisit_strips_fragments_before_storing() =
        runTest(testDispatcher) {
            repository.recordVisit("https://example.com/recipe#ingredients")
            repository.observeRecent().test {
                awaitItem()[0].url shouldBe "https://example.com/recipe"
            }
        }

    @Test
    fun recordVisit_skips_default_landing_url() =
        runTest(testDispatcher) {
            repository.recordVisit("https://www.google.com")
            repository.observeRecent().test { awaitItem().size shouldBe 0 }
        }

    @Test
    fun recordVisit_skips_google_search_pages() =
        runTest(testDispatcher) {
            repository.recordVisit("https://www.google.com/search?q=lasagna")
            repository.observeRecent().test { awaitItem().size shouldBe 0 }
        }

    @Test
    fun deleteById_removes_only_that_row() =
        runTest(testDispatcher) {
            repository.recordVisit("https://example.com/a")
            repository.recordVisit("https://example.com/b")
            val all = db.browserHistoryQueries.getRecent(50).executeAsList()
            val toDelete = all.first { it.url == "https://example.com/a" }
            repository.deleteById(toDelete.id)
            repository.observeRecent().test {
                val items = awaitItem()
                items.size shouldBe 1
                items[0].url shouldBe "https://example.com/b"
            }
        }

    @Test
    fun clearAll_removes_all_rows() =
        runTest(testDispatcher) {
            repository.recordVisit("https://example.com/a")
            repository.recordVisit("https://example.com/b")
            repository.clearAll()
            repository.observeRecent().test { awaitItem().size shouldBe 0 }
        }
}
