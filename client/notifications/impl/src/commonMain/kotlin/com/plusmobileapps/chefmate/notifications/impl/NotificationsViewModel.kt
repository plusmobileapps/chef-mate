package com.plusmobileapps.chefmate.notifications.impl

import chefmate.client.notifications.public.generated.resources.Res
import chefmate.client.notifications.public.generated.resources.notifications_accept_error
import chefmate.client.notifications.public.generated.resources.notifications_already_in_family
import chefmate.client.notifications.public.generated.resources.notifications_decline_error
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.family.data.AlreadyInFamilyException
import com.plusmobileapps.chefmate.notifications.NotificationsBloc.Model
import com.plusmobileapps.chefmate.notifications.data.AppNotification
import com.plusmobileapps.chefmate.notifications.data.NotificationsRepository
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.toast.ToastService
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class NotificationsViewModel(
    @Main mainContext: CoroutineContext,
    private val repository: NotificationsRepository,
    authenticationRepository: AuthenticationRepository,
    private val toastService: ToastService,
) : ViewModel(mainContext) {

    private val processing = MutableStateFlow(persistentSetOf<String>())

    val state: StateFlow<Model> =
        combine(repository.notifications, authenticationRepository.state, processing) {
                notifications,
                authState,
                inFlight ->
                Model(
                    notifications = notifications.toImmutableList(),
                    isLoading = false,
                    isSignedIn =
                        (authState as? AuthState.Authenticated)?.user?.isAnonymous == false,
                    processing = inFlight,
                )
            }
            .stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), Model())

    init {
        // Recipe-book invites are one-shot; kick a fetch each time the screen is entered so the
        // list is current even if it went stale since the badge last refreshed.
        repository.refresh()
    }

    fun accept(notification: AppNotification) {
        act(notification, Res.string.notifications_accept_error) { repository.accept(it) }
    }

    fun decline(notification: AppNotification) {
        act(notification, Res.string.notifications_decline_error) { repository.decline(it) }
    }

    private fun act(
        notification: AppNotification,
        errorMessage: org.jetbrains.compose.resources.StringResource,
        action: suspend (AppNotification) -> Unit,
    ) {
        if (notification.key in processing.value) return
        processing.update { it.add(notification.key) }
        scope.launch {
            runCatching { action(notification) }
                .onFailure { error ->
                    // A user can only belong to one family, so this failure is actionable — tell
                    // them what to do rather than showing the generic "try again".
                    val message =
                        if (error is AlreadyInFamilyException) {
                            Res.string.notifications_already_in_family
                        } else {
                            errorMessage
                        }
                    toastService.show(message.asTextData())
                }
            processing.update { it.remove(notification.key) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5000L
    }
}
