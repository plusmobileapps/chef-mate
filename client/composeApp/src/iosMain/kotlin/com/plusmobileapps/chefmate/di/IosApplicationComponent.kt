package com.plusmobileapps.chefmate.di

import com.plusmobileapps.chefmate.ApplicationComponent
import com.plusmobileapps.chefmate.client.database.DriverFactory
import com.plusmobileapps.chefmate.util.DateTimeFormatterUtil
import com.plusmobileapps.chefmate.util.DateTimeFormatterUtilImpl
import me.tatarka.inject.annotations.Provides
import platform.UIKit.UIApplication
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.reflect.KClass

@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
abstract class IosApplicationComponent(
    @get:Provides val application: UIApplication,
) : ApplicationComponent {
    @Provides
    fun driverFactory(): DriverFactory = DriverFactory()

    @Provides
    @SingleIn(AppScope::class)
    fun provideDateTimeFormatterUtil(): DateTimeFormatterUtil = DateTimeFormatterUtilImpl()
}

/**
 * The `actual fun` will be generated for each iOS specific target. See [MergeComponent] for
 * more details.
 */
@MergeComponent.CreateComponent
expect fun KClass<IosApplicationComponent>.createIosAppComponent(application: UIApplication): IosApplicationComponent
