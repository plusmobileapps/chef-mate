@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

// wasmJs (browser) has no IO dispatcher; Default is the closest equivalent.
actual val ioDispatcher: CoroutineContext = Dispatchers.Default
