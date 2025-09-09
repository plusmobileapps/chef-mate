package com.plusmobileapps.chefmate

import android.app.Application
import com.plusmobileapps.chefmate.di.AndroidApplicationComponent
import com.plusmobileapps.chefmate.di.create

class MyApplication : Application() {

    lateinit var appComponent: AndroidApplicationComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = AndroidApplicationComponent::class.create(this)
    }
}