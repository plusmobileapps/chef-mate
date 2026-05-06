package com.plusmobileapps.chefmate.testing

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class E2eTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, name: String, context: Context): Application =
        Instrumentation.newApplication(TestApplication::class.java, context)
}
