package com.plusmobileapps.chefmate.deeplink

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Funnels desktop deep links from every runtime source to the running UI:
 * - the macOS `Desktop.setOpenURIHandler` Apple Event (main.kt),
 * - the Windows/Linux single-instance socket ([SingleInstance]) when a second launch forwards a
 *   link.
 *
 * The cold-start link (first process argument) is handled separately as the initial navigation
 * stack in `main`, so it does not flow through here — this coordinator carries only links that
 * arrive *at* or *after* launch.
 *
 * `replay = 1` covers the cold-launch race on macOS, where `open chefmate://…` starts the app and
 * then delivers the URL as an Apple Event a moment later — potentially before the Compose collector
 * is attached. The replayed value is delivered to the first collector so the link isn't dropped.
 * `extraBufferCapacity` lets the non-coroutine callers (AWT event thread, socket thread) [submit]
 * without suspending.
 */
object DeepLinkCoordinator {
    private val _links = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 16)

    /** Deep-link URLs (e.g. `chefmate://notifications`) as they arrive. */
    val links: SharedFlow<String> = _links.asSharedFlow()

    /** Non-suspending; safe to call from any thread. Null/blank links are ignored. */
    fun submit(url: String?) {
        val trimmed = url?.trim().orEmpty()
        if (trimmed.isEmpty()) return
        _links.tryEmit(trimmed)
    }
}
