package com.plusmobileapps.chefmate

import kotlin.coroutines.CoroutineContext

/**
 * Dispatcher for blocking IO work. JVM/Android/iOS use [kotlinx.coroutines.Dispatchers.IO]; wasmJs
 * has no IO dispatcher (single-threaded browser runtime) and falls back to `Dispatchers.Default`.
 */
expect val ioDispatcher: CoroutineContext
