package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Phone side of the watch companion. The phone is the source of truth: the watch holds no
 * Kotlin/Supabase, so this bridges the shared [GroceryRepository] to the watch over
 * WatchConnectivity. It emits a JSON snapshot of all lists + items (which the Swift
 * `WatchDataSender` pushes to the watch), and applies the watch's toggle/add actions (which sync to
 * Supabase).
 *
 * Everything is exposed as plain primitives / JSON so the Swift glue never touches Kotlin models.
 */
@Inject
@SingleIn(AppScope::class)
class WatchDataBridge(
    private val repository: GroceryRepository,
    @IO private val ioContext: CoroutineContext,
) {
    private val scope = CoroutineScope(SupervisorJob() + ioContext)
    private val json = Json { encodeDefaults = true }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val snapshotFlow: Flow<String> =
        repository.getGroceryLists().flatMapLatest { lists ->
            if (lists.isEmpty()) {
                flowOf(encode(emptyList()))
            } else {
                combine(lists.map { list -> repository.getGroceries(list.id).map { list to it } }) {
                    encode(it.toList())
                }
            }
        }

    /** Streams a JSON snapshot whenever the grocery data changes. Returns a cancel function. */
    fun observeSnapshot(onSnapshot: (String) -> Unit): () -> Unit {
        val job = scope.launch { snapshotFlow.collect { onSnapshot(it) } }
        return { job.cancel() }
    }

    /** Delivers the current JSON snapshot once (for answering a watch pull request). */
    fun currentSnapshot(onResult: (String) -> Unit) {
        scope.launch { onResult(snapshotFlow.first()) }
    }

    fun addItem(listId: Long, name: String) {
        scope.launch {
            repository.addGrocery(listId, name)
            repository.syncAllUnsynced()
        }
    }

    fun setChecked(itemId: Long, isChecked: Boolean) {
        scope.launch {
            repository.getGrocery(itemId)?.let { repository.updateChecked(it, isChecked) }
        }
    }

    /** Local id of the default list, creating one if needed (used by the Siri "add" intent). */
    fun ensureDefaultList(onResult: (Long) -> Unit) {
        scope.launch { onResult(repository.ensureDefaultList()) }
    }

    private fun encode(listToItems: List<Pair<GroceryListModel, List<GroceryItem>>>): String {
        val lists = listToItems.map { (list, _) -> WatchListDto(list.id, list.name, list.isShared) }
        val items = listToItems.flatMap { (list, items) ->
            items.map {
                WatchItemDto(
                    id = it.id,
                    listId = list.id,
                    name = it.displayName,
                    quantity = it.quantity,
                    category = it.category.name,
                    isChecked = it.isChecked,
                )
            }
        }
        return json.encodeToString(WatchSnapshot(lists, items))
    }
}

@Serializable
private data class WatchSnapshot(val lists: List<WatchListDto>, val items: List<WatchItemDto>)

@Serializable private data class WatchListDto(val id: Long, val name: String, val isShared: Boolean)

@Serializable
private data class WatchItemDto(
    val id: Long,
    val listId: Long,
    val name: String,
    val quantity: String?,
    val category: String,
    val isChecked: Boolean,
)
