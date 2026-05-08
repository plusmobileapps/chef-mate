package com.plusmobileapps.chefmate.devsettings.impl

import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import com.plusmobileapps.chefmate.devsettings.TestUser
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class TestUserProvider {
    val users: List<TestUser> = parse(BuildConfig.TEST_USERS)

    companion object {
        fun parse(serialized: String): List<TestUser> =
            if (serialized.isBlank()) {
                emptyList()
            } else {
                serialized
                    .split(';')
                    .filter { it.isNotBlank() }
                    .mapIndexedNotNull { i, entry ->
                        val parts = entry.split('|', limit = 2)
                        if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                            null
                        } else {
                            TestUser(index = i + 1, email = parts[0], password = parts[1])
                        }
                    }
            }
    }
}
