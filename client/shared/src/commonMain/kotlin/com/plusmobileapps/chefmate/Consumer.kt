package com.plusmobileapps.chefmate

/**
 * A simple consumer interface that can be used as a callback or listener in BLoCs.
 */
fun interface Consumer<T> {
    fun onNext(output: T)
}
