package com.plusmobileapps.chefmate

import android.app.Application

class MyApplication : Application() {

    lateinit var appComponent: AndroidApplicationComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = AndroidApplicationComponent(this)
    }
}