package com.plusmobileapps.chefmate.di

import com.plusmobileapps.chefmate.client.database.di.DatabaseComponent
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph

@SingleIn(AppScope::class)
@DependencyGraph(scope = AppScope::class, excludes = [DatabaseComponent::class])
abstract class IosTestApplicationComponent : BaseTestApplicationComponent()

actual fun createTestApplicationComponent(): TestApplicationComponent =
    createGraph<IosTestApplicationComponent>()
