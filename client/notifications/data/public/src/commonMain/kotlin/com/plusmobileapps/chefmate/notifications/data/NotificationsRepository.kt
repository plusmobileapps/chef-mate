package com.plusmobileapps.chefmate.notifications.data

import kotlinx.coroutines.flow.Flow

/**
 * Aggregates the current user's in-app notifications from the underlying feature repositories
 * (grocery + recipe-book collaboration). Emits an empty list when signed out or when nothing is
 * pending. The same [notifications] stream backs both the Notifications screen and the More-tab
 * badge count.
 */
interface NotificationsRepository {
    /**
     * The current notifications, newest sources merged. Re-emits after [refresh] and on sign-in.
     */
    val notifications: Flow<List<AppNotification>>

    /** Accepts the invite behind [notification], then refreshes. Throws on failure. */
    suspend fun accept(notification: AppNotification)

    /** Declines the invite behind [notification], then refreshes. Throws on failure. */
    suspend fun decline(notification: AppNotification)

    /** Force [notifications] to re-fetch from its sources (e.g. on screen open or app resume). */
    fun refresh()
}
