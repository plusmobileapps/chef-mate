package com.plusmobileapps.chefmate

import android.content.Context
import com.plusmobileapps.chefmate.client.database.DriverFactory

class AndroidApplicationComponent(context: Context) :
    ApplicationComponent by DefaultApplicationComponent(
        driverFactory = DriverFactory(context = context)
    )