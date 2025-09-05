package com.plusmobileapps.chefmate

import android.app.Application
import com.plusmobileapps.chefmate.client.database.DriverFactory
import com.plusmobileapps.chefmate.client.database.createDatabase
import com.plusmobileapps.chefmate.database.Database

class MyApplication : Application() {
    lateinit var database: Database

    override fun onCreate() {
        super.onCreate()
        database = createDatabase(DriverFactory(this))
    }

}