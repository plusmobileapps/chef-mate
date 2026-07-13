package com.plusmobileapps.chefmate.notifications.data.testing

import com.plusmobileapps.chefmate.notifications.data.AppNotification
import com.plusmobileapps.chefmate.notifications.data.NotificationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNotificationsRepository(
    initial: List<AppNotification> = emptyList(),
    /** When set, accept/decline throw it so tests can exercise the error path. */
    var failWith: Throwable? = null,
) : NotificationsRepository {

    val notificationsState = MutableStateFlow(initial)
    override val notifications: Flow<List<AppNotification>> = notificationsState

    val accepted: MutableList<AppNotification> = mutableListOf()
    val declined: MutableList<AppNotification> = mutableListOf()
    var refreshCount: Int = 0
        private set

    override suspend fun accept(notification: AppNotification) {
        failWith?.let { throw it }
        accepted += notification
        notificationsState.value = notificationsState.value.filterNot { it.key == notification.key }
    }

    override suspend fun decline(notification: AppNotification) {
        failWith?.let { throw it }
        declined += notification
        notificationsState.value = notificationsState.value.filterNot { it.key == notification.key }
    }

    override fun refresh() {
        refreshCount++
    }
}
