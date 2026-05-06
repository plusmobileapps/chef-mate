package com.plusmobileapps.chefmate.testing

import android.app.Application

/**
 * Empty Application used by instrumented tests to avoid running MyApplication.onCreate (Bugsnag
 * init, real DI graph creation). Tests build their own [TestAndroidApplicationComponent] instead.
 */
class TestApplication : Application()
