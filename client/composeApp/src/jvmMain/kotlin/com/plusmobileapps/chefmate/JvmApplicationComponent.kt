package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.client.database.DriverFactory
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
abstract class JvmApplicationComponent : ApplicationComponent {
    @Provides
    fun providesDriverFactory(): DriverFactory = DriverFactory()
}
