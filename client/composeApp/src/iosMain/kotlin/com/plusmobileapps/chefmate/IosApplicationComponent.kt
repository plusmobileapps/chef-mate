package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.client.database.DriverFactory

class IosApplicationComponent : ApplicationComponent by DefaultApplicationComponent(
    driverFactory = DriverFactory()
)