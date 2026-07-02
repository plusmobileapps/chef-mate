package com.plusmobileapps.chefmate.watch

import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Swift-facing entry point for the watchOS app. Wraps the shared
 * [GroceryRepository] + [AuthenticationRepository] so the native SwiftUI UI, and Siri App Intents,
 * drive the same offline-first, Supabase-backed data layer the phone uses.
 *
 * Reads are exposed as callback-based `observe*` methods (returning a [WatchCancellable]) rather
 * than Kotlin `Flow`s, which don't bridge cleanly to Swift. Mutations are `suspend` functions,
 * which map to Swift `async` — convenient for `AppIntent.perform()`.
 */
@Inject
@SingleIn(AppScope::class)
class WatchGroceryController(
    private val repository: GroceryRepository,
    private val authRepository: AuthenticationRepository,
    private val sessionImporter: WatchSessionImporter,
    @IO private val ioContext: CoroutineContext,
) {
    private val scope = CoroutineScope(SupervisorJob() + ioContext)

    /** Emits `true` once a Supabase session exists (real or anonymous), else `false`. */
    fun observeSignedIn(onEach: (Boolean) -> Unit): WatchCancellable {
        val job = scope.launch {
            authRepository.state.map { it is AuthState.Authenticated }.collect { onEach(it) }
        }
        return WatchCancellable { job.cancel() }
    }

    fun observeLists(onEach: (List<WatchGroceryList>) -> Unit): WatchCancellable {
        val job = scope.launch {
            repository.getGroceryLists().collect { lists -> onEach(lists.map { it.toWatch() }) }
        }
        return WatchCancellable { job.cancel() }
    }

    fun observeItems(listId: Long, onEach: (List<WatchGroceryItem>) -> Unit): WatchCancellable {
        val job = scope.launch {
            repository.getGroceries(listId).collect { items -> onEach(items.map { it.toWatch() }) }
        }
        return WatchCancellable { job.cancel() }
    }

    /** Returns the local id of the user's default list, creating one if none exists. */
    suspend fun ensureDefaultList(): Long = repository.ensureDefaultList()

    suspend fun addItem(listId: Long, name: String) {
        repository.addGrocery(listId, name)
    }

    suspend fun setChecked(itemId: Long, isChecked: Boolean) {
        val item = repository.getGrocery(itemId) ?: return
        repository.updateChecked(item, isChecked)
    }

    /** Pushes any local changes and pulls remote updates. Call on app activation / after edits. */
    suspend fun syncNow() {
        repository.syncAllUnsynced()
    }

    /**
     * Adopts a Supabase session handed off from the phone over WatchConnectivity, which flips
     * [AuthenticationRepository.state] to Authenticated and triggers the repository's existing
     * auth-state-driven sync. See [WatchSessionImporter].
     */
    suspend fun importSession(refreshToken: String) {
        sessionImporter.importSession(refreshToken)
    }

    suspend fun signOut() {
        authRepository.signOut()
    }

    private fun GroceryListModel.toWatch() =
        WatchGroceryList(id = id, name = name, isShared = isShared)

    private fun GroceryItem.toWatch() =
        WatchGroceryItem(
            id = id,
            name = displayName,
            quantity = quantity,
            category = category.name,
            isChecked = isChecked,
        )
}
