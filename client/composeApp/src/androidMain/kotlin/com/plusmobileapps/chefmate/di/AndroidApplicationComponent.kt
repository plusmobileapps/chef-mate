package com.plusmobileapps.chefmate.di

import android.content.Context
import com.plusmobileapps.chefmate.ApplicationComponent
import com.plusmobileapps.chefmate.client.database.DriverFactory
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
abstract class AndroidApplicationComponent(
    @get:Provides val context: Context,
) : ApplicationComponent {
    @Provides
    fun driverFactory(): DriverFactory = DriverFactory(context = context)
}
