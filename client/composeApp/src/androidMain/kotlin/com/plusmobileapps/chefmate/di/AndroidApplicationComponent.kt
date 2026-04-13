package com.plusmobileapps.chefmate.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.plusmobileapps.chefmate.ApplicationComponent
import com.plusmobileapps.chefmate.client.database.DriverFactory
import com.plusmobileapps.chefmate.util.DateTimeFormatterUtil
import com.plusmobileapps.chefmate.util.DateTimeFormatterUtilImpl
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@DependencyGraph(AppScope::class)
@SingleIn(AppScope::class)
abstract class AndroidApplicationComponent : ApplicationComponent {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides context: Context,
        ): AndroidApplicationComponent
    }

    @Provides
    fun driverFactory(context: Context): DriverFactory = DriverFactory(context = context)

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @SingleIn(AppScope::class)
    fun provideDateTimeFormatterUtil(): DateTimeFormatterUtil = DateTimeFormatterUtilImpl()
}
