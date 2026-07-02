package com.plusmobileapps.chefmate.watch

import com.plusmobileapps.chefmate.Environment
import com.plusmobileapps.chefmate.EnvironmentProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fixed [EnvironmentProvider] for the watch. The production provider comes from the Compose
 * `developer-settings` module (not on the watch classpath), so the watch always talks to the same
 * production Supabase backend as the phone. The watch never exposes an environment switcher.
 */
class WatchEnvironmentProvider : EnvironmentProvider {
    override val environment: StateFlow<Environment> = MutableStateFlow(Environment.PROD)
}
