package com.plusmobileapps.chefmate.util.impl

import com.plusmobileapps.chefmate.util.DateTimeUtil
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import kotlin.time.Clock
import kotlin.time.Instant

@Inject
@ContributesBinding(AppScope::class, boundType = DateTimeUtil::class)
class RealDateTimeUtil : DateTimeUtil {
    override val now: Instant
        get() = Clock.System.now()
}