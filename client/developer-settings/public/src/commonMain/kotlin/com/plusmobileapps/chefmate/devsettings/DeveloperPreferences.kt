package com.plusmobileapps.chefmate.devsettings

import com.plusmobileapps.chefmate.Environment
import com.plusmobileapps.chefmate.EnvironmentProvider
import kotlinx.coroutines.flow.StateFlow

const val DEV_ENVIRONMENT_KEY = "dev.environment"
const val DEV_USER_INDEX_KEY = "dev.user_index"

interface DeveloperPreferences : EnvironmentProvider {
    override val environment: StateFlow<Environment>

    val selectedUserIndex: StateFlow<Int?>

    fun setEnvironment(environment: Environment)

    fun setSelectedUserIndex(index: Int?)
}
