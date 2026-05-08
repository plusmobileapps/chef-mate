package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.client.database.DriverFactory
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@DependencyGraph(AppScope::class)
abstract class JvmApplicationComponent : ApplicationComponent {
    @Provides fun providesDriverFactory(): DriverFactory = DriverFactory()
}
