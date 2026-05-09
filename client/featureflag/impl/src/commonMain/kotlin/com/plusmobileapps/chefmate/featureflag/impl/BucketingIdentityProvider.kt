@file:OptIn(ExperimentalUuidApi::class)

package com.plusmobileapps.chefmate.featureflag.impl

import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.mapState
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.StateFlow

@Inject
@SingleIn(AppScope::class)
class BucketingIdentityProvider(
    authRepository: AuthenticationRepository,
    private val settings: Settings,
) {
    private val deviceId: String = ensureDeviceId()

    val identity: StateFlow<String> =
        authRepository.state.mapState { state ->
            when (state) {
                is AuthState.Authenticated -> state.user.userId
                is AuthState.AwaitingEmailVerification,
                AuthState.Unauthenticated -> deviceId
            }
        }

    private fun ensureDeviceId(): String {
        settings.getStringOrNull(KEY)?.let {
            return it
        }
        val generated = Uuid.random().toString()
        settings.putString(KEY, generated)
        return generated
    }

    private companion object {
        const val KEY = "flags.bucket_id"
    }
}
