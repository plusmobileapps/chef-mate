package com.plusmobileapps.chefmate.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.notifications.data.AppNotification
import com.plusmobileapps.chefmate.notifications.ui.NotificationsScreen
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.StateFlow

interface NotificationsBloc : ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        NotificationsScreen(bloc = this, modifier = modifier)
    }

    fun onBack()

    fun onAccept(notification: AppNotification)

    fun onDecline(notification: AppNotification)

    data class Model(
        val notifications: ImmutableList<AppNotification> = persistentListOf(),
        val isLoading: Boolean = true,
        /**
         * False when there's no real (non-anonymous) session. Invites are addressed to an account
         * email, so a signed-out/anonymous user has none to act on — the screen shows a sign-in
         * hint instead of an empty state.
         */
        val isSignedIn: Boolean = true,
        /**
         * [AppNotification.key]s with an accept/decline in flight, used to disable their buttons.
         */
        val processing: ImmutableSet<String> = persistentSetOf(),
    )

    sealed class Output {
        /** Pop back to the More tab. */
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): NotificationsBloc
    }
}
