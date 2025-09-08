package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.client.database.DriverFactory

class JvmApplicationComponent : ApplicationComponent by DefaultApplicationComponent(
    driverFactory = DriverFactory()
)