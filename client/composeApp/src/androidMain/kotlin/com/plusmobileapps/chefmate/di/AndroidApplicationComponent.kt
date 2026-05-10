package com.plusmobileapps.chefmate.di

import android.content.Context
import com.plusmobileapps.chefmate.ApplicationComponent
import com.plusmobileapps.chefmate.auth.data.impl.CurrentActivityHolder
import com.plusmobileapps.chefmate.auth.data.impl.GoogleSignInProvider
import com.plusmobileapps.chefmate.client.database.DriverFactory
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@DependencyGraph(AppScope::class)
@SingleIn(AppScope::class)
abstract class AndroidApplicationComponent : ApplicationComponent {
    abstract val currentActivityHolder: CurrentActivityHolder

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): AndroidApplicationComponent
    }

    @Provides fun driverFactory(context: Context): DriverFactory = DriverFactory(context = context)

    @Provides
    @SingleIn(AppScope::class)
    fun provideGoogleSignInProvider(
        context: Context,
        activityHolder: CurrentActivityHolder,
    ): GoogleSignInProvider =
        GoogleSignInProvider(appContext = context, activityHolder = activityHolder)
}
