package com.plusmobileapps.chefmate.watch

/**
 * Swift-friendly entry point. Building the Metro graph and reaching for the controller in one call
 * avoids Swift having to touch the generated graph companion. Call once from the SwiftUI app and
 * hold the returned [WatchGroceryController] for the process lifetime.
 *
 * Swift: `WatchEntryPointKt.createWatchGroceryController()`.
 */
fun createWatchGroceryController(): WatchGroceryController =
    WatchApplicationComponent.create().groceryController
