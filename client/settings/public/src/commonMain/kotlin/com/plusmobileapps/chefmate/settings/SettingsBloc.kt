package com.plusmobileapps.chefmate.settings

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.coroutines.flow.StateFlow

interface SettingsBloc : ComposeScreen {
    val state: StateFlow<Model>

    fun onSignInClicked()

    fun onSignUpClicked()

    fun onSignOutClicked()

    fun onSignOutConfirmed()

    fun onSignOutDismissed()

    fun onManageProfileClicked()

    fun onUrlClicked(url: String)

    fun onAppSettingsClicked()

    fun onAiChatClicked()

    fun onDeveloperSettingsClicked()

    fun onReplayOnboardingClicked()

    data class Model(
        val isAuthenticated: Boolean = false,
        /**
         * True when the user is signed in via Supabase anonymous auth. Surfaced separately from
         * [isAuthenticated] because the UI shows neither a greeting nor a Sign-Out row in this
         * state — anonymous sessions have no credentials to come back to, so "Sign Out" would
         * silently orphan their data instead of doing what users expect.
         */
        val isAnonymous: Boolean = false,
        val greeting: TextData? = null,
        val verificationMessage: TextData? = null,
        val showSignOutConfirmationDialog: Boolean = false,
        val isDebugBuild: Boolean = false,
        val isAiChatEnabled: Boolean = false,
        /** Gates the "View Onboarding Again" row behind the onboarding feature flag. */
        val isOnboardingEnabled: Boolean = false,
        val versionName: String = "",
    )

    sealed class Output {
        data object OpenSignUp : Output()

        data object OpenSignIn : Output()

        data object OpenManageProfile : Output()

        data object OpenAppSettings : Output()

        data object OpenAiChat : Output()

        data object OpenDeveloperSettings : Output()

        /** Replay the first-run onboarding flow from the More tab. */
        data object OpenOnboarding : Output()

        data class OpenUrl(val url: String) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): SettingsBloc
    }
}
