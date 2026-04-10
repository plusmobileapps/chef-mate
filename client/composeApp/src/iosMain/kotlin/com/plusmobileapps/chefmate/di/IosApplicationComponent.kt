package com.plusmobileapps.chefmate.di

import com.plusmobileapps.chefmate.ApplicationComponent
import com.plusmobileapps.chefmate.client.database.DriverFactory
import com.plusmobileapps.chefmate.util.DateTimeFormatterUtil
import com.plusmobileapps.chefmate.util.DateTimeFormatterUtilImpl
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import platform.UIKit.UIApplication

@SingleIn(AppScope::class)
@DependencyGraph(AppScope::class)
abstract class IosApplicationComponent : ApplicationComponent {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: UIApplication): IosApplicationComponent
    }

    @Provides
    fun driverFactory(): DriverFactory = DriverFactory()

    @Provides
    @SingleIn(AppScope::class)
    fun provideDateTimeFormatterUtil(): DateTimeFormatterUtil = DateTimeFormatterUtilImpl()
}
