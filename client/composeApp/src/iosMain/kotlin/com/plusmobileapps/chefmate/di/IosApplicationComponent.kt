package com.plusmobileapps.chefmate.di

import com.plusmobileapps.chefmate.ApplicationComponent
import com.plusmobileapps.chefmate.WatchSessionRelay
import com.plusmobileapps.chefmate.client.database.DriverFactory
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import platform.UIKit.UIApplication

@SingleIn(AppScope::class)
@DependencyGraph(AppScope::class)
abstract class IosApplicationComponent : ApplicationComponent {
    /** Bridge for handing the Supabase session to the watch companion. */
    abstract val watchSessionRelay: WatchSessionRelay

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: UIApplication): IosApplicationComponent
    }

    @Provides fun driverFactory(): DriverFactory = DriverFactory()
}
