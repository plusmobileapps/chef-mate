package com.plusmobileapps.chefmate.di

import com.plusmobileapps.chefmate.mapState
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Coordinates first-run "coach mark" tooltips so that at most one is ever visible across the whole
 * app at a time.
 *
 * A screen [request]s its coach mark when the anchor enters composition; the controller grants
 * visibility to a single mark at a time via [activeCoachMark], first-come-first-served. When the
 * visible mark is [dismiss]ed it is persisted as seen (multiplatform-settings) and never requested
 * again, and the next queued mark — if any anchor is still on screen — becomes active.
 *
 * [release] drops a still-unseen request (e.g. its anchor left the screen without being dismissed)
 * so it stops blocking the queue; it can be requested again on a later visit.
 *
 * Identifiers come from [CoachMarkId].
 */
@Inject
@SingleIn(AppScope::class)
class CoachMarkController(private val settings: Settings) {

    /** Pending coach-mark ids in request order; the head is the one currently allowed to show. */
    private val queue = MutableStateFlow<List<String>>(emptyList())

    /** Id of the coach mark currently allowed to show, or null when none is pending. */
    val activeCoachMark: StateFlow<String?> = queue.mapState { it.firstOrNull() }

    /** Enqueue [id] to be shown when it reaches the head of the queue, unless already seen. */
    fun request(id: String) {
        if (hasSeen(id)) return
        queue.update { current -> if (id in current) current else current + id }
    }

    /** Drop [id] from the queue without marking it seen — it may be requested again later. */
    fun release(id: String) {
        queue.update { current -> current - id }
    }

    /** Mark [id] as seen forever and remove it from the queue, letting the next mark show. */
    fun dismiss(id: String) {
        settings.putBoolean(seenKey(id), true)
        queue.update { current -> current - id }
    }

    fun hasSeen(id: String): Boolean = settings.getBoolean(seenKey(id), false)

    private fun seenKey(id: String): String = "$SEEN_KEY_PREFIX$id"

    companion object {
        private const val SEEN_KEY_PREFIX = "coach_mark_seen_"
    }
}
