package com.plusmobileapps.chefmate.testing

import com.plusmobileapps.chefmate.Consumer

class TestConsumer<T> : Consumer<T> {
    val values = mutableListOf<T>()

    val lastValue: T
        get() = values.last()

    override fun onNext(output: T) {
        values.add(output)
    }
}
