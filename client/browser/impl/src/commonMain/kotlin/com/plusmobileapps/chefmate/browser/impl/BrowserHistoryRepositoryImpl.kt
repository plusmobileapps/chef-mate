package com.plusmobileapps.chefmate.browser.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.plusmobileapps.chefmate.browser.BROWSER_HISTORY_ENABLED_KEY
import com.plusmobileapps.chefmate.browser.BrowserHistoryEntry
import com.plusmobileapps.chefmate.browser.BrowserHistoryRepository
import com.plusmobileapps.chefmate.database.BrowserHistoryQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class BrowserHistoryRepositoryImpl(
    private val queries: BrowserHistoryQueries,
    private val settings: Settings,
    @IO private val ioContext: CoroutineContext,
) : BrowserHistoryRepository {

    override fun observeRecent(limit: Int): Flow<List<BrowserHistoryEntry>> =
        queries.getRecent(limit.toLong()).asFlow().mapToList(ioContext).map { rows ->
            rows.map { BrowserHistoryEntry(it.id, it.url, it.visitedAt) }
        }

    override suspend fun recordVisit(url: String) {
        if (!settings.getBoolean(BROWSER_HISTORY_ENABLED_KEY, defaultValue = true)) return
        val normalized = UrlNormalizer.normalize(url) ?: return
        withContext(ioContext) { queries.recordVisit(normalized) }
    }

    override suspend fun deleteById(id: Long) {
        withContext(ioContext) { queries.deleteById(id) }
    }

    override suspend fun clearAll() {
        withContext(ioContext) { queries.deleteAll() }
    }
}
