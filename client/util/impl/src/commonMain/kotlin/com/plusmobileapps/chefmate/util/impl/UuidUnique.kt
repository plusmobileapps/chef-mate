package com.plusmobileapps.chefmate.util.impl

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.util.Unique
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Inject
@ContributesBinding(AppScope::class)
class UuidUnique : Unique {
    override fun generate(): String = Uuid.random().toString()
}
