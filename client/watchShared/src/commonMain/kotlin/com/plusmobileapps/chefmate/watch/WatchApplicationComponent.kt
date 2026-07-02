package com.plusmobileapps.chefmate.watch

import com.plusmobileapps.chefmate.EnvironmentProvider
import com.plusmobileapps.chefmate.client.database.DriverFactory
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.util.DateTimeUtil
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph

/**
 * The watch app's Metro dependency graph — the slim, Compose-free analogue of
 * `IosApplicationComponent`. It aggregates the `@ContributesTo`/`@ContributesBinding` bindings from
 * the data modules on its classpath (SupabaseModule, DatabaseComponent, CoroutinesComponent,
 * SettingsComponent, the grocery/auth repositories) and supplies the three leaf dependencies those
 * bindings need but that normally come from Compose-coupled modules absent on watchOS:
 * [DriverFactory], [EnvironmentProvider], and [DateTimeUtil].
 */
@SingleIn(AppScope::class)
@DependencyGraph(AppScope::class)
interface WatchApplicationComponent {
    val groceryController: WatchGroceryController

    @Provides fun driverFactory(): DriverFactory = DriverFactory()

    @Provides
    @SingleIn(AppScope::class)
    fun environmentProvider(): EnvironmentProvider = WatchEnvironmentProvider()

    @Provides @SingleIn(AppScope::class) fun dateTimeUtil(): DateTimeUtil = WatchDateTimeUtil()

    companion object {
        fun create(): WatchApplicationComponent = createGraph()
    }
}
