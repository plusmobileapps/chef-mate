package com.plusmobileapps.chefmate.browser

import kotlinx.coroutines.flow.Flow

const val BROWSER_HISTORY_ENABLED_KEY = "browser.history.enabled"

const val BROWSER_SEARCH_ENGINE_KEY = "browser.search.engine"

data class BrowserHistoryEntry(val id: Long, val url: String, val visitedAt: String)

interface BrowserHistoryRepository {
    fun observeRecent(limit: Int = 50): Flow<List<BrowserHistoryEntry>>

    suspend fun recordVisit(url: String)

    suspend fun deleteById(id: Long)

    suspend fun clearAll()
}
