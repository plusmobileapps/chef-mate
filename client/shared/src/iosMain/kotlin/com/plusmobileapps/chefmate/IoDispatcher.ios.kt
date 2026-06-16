@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual val ioDispatcher: CoroutineContext = Dispatchers.IO
