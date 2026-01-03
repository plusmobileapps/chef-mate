package com.plusmobileapps.chefmate

import android.app.Application
import com.plusmobileapps.chefmate.di.AndroidApplicationComponent
import com.plusmobileapps.chefmate.di.create
import com.russhwolf.settings.BuildConfig
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier

class MyApplication : Application() {
    lateinit var appComponent: AndroidApplicationComponent

    override fun onCreate() {
        super.onCreate()
        Napier.base(
            if (BuildConfig.DEBUG) {
                DebugAntilog()
            } else {
                object : io.github.aakira.napier.Antilog() {
                    override fun performLog(
                        priority: LogLevel,
                        tag: String?,
                        throwable: Throwable?,
                        message: String?
                    ) = Unit
                }
            }
        )
        appComponent = AndroidApplicationComponent::class.create(this)
    }
}
