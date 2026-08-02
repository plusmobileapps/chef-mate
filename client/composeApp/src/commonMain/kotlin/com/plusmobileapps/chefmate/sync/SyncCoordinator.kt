package com.plusmobileapps.chefmate.sync

import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.meal.data.MealPlanRepository
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRepository
import com.plusmobileapps.chefmate.util.DateTimeUtil
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Runs a full reconcile across every syncing repository, in dependency order.
 *
 * Repositories sync themselves whenever a session arrives or the user taps sync, which is enough on
 * mobile — the OS tears the process down and the next launch reconciles. A desktop process instead
 * stays up for days, so this exists to give that process something to call when it has reason to
 * believe it fell behind (the window regaining focus, a periodic tick). See
 * `AuthenticationRepository.refreshSessionIfNeeded` for what "fell behind" usually means.
 */
@Inject
@SingleIn(AppScope::class)
class SyncCoordinator(
    private val authRepository: AuthenticationRepository,
    private val recipeBookRepository: RecipeBookRepository,
    private val recipeRepository: RecipeRepository,
    private val groceryRepository: GroceryRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val dateTimeUtil: DateTimeUtil,
    @IO private val ioContext: CoroutineContext,
) {

    private val mutex = Mutex()
    private var lastAttemptAt: Instant? = null

    /**
     * Reconciles every repository, unless a run already happened within [MIN_INTERVAL] — focus
     * events in particular can arrive in bursts as the user moves between windows. Pass [force] to
     * bypass that (a user-initiated sync should never be swallowed).
     *
     * Throttling counts failed attempts too, so a machine that's genuinely offline isn't retried on
     * every alt-tab. The per-screen sync buttons stay available as an immediate escape hatch.
     */
    suspend fun syncAll(force: Boolean = false): SyncOutcome = mutex.withLock {
        if (authRepository.state.value !is AuthState.Authenticated) return SyncOutcome.SignedOut

        val now = dateTimeUtil.now
        val last = lastAttemptAt
        if (!force && last != null && now - last < MIN_INTERVAL) return SyncOutcome.Throttled
        lastAttemptAt = now

        // Nothing below can succeed on a dead token, and this is the moment we're best placed
        // to revive it — a wake-from-sleep is exactly when the SDK's refresh timer has slipped.
        if (!authRepository.refreshSessionIfNeeded()) return SyncOutcome.SessionExpired

        withContext(ioContext) {
            // Books before recipes: a recipe resolves its book by remote id, so a book that
            // hasn't been pushed yet would strand the recipes filed under it.
            recipeBookRepository.syncAllUnsynced()
            recipeRepository.syncAllUnsynced()
            groceryRepository.syncAllUnsynced()
            mealPlanRepository.syncAllUnsynced()
        }
        SyncOutcome.Synced
    }

    private companion object {
        val MIN_INTERVAL = 1.minutes
    }
}

/** Why a [SyncCoordinator.syncAll] call did or didn't do any work. */
enum class SyncOutcome {
    Synced,

    /** Nothing to sync — no one is signed in. */
    SignedOut,

    /** A run happened recently enough that this one was skipped. */
    Throttled,

    /**
     * The access token is dead and couldn't be renewed, so syncing was not attempted. Worth
     * surfacing: from the user's side this is indistinguishable from the app quietly doing nothing.
     */
    SessionExpired,
}
