package com.plusmobileapps.chefmate

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate

fun <T : ViewModel> InstanceKeeper.getViewModel(
    key: Any,
    factory: () -> T,
): T =
    getOrCreate(key) { ViewModelHolder(factory()) }
        .viewModel

inline fun <reified T : ViewModel> InstanceKeeper.getViewModel(noinline factory: () -> T): T =
    getViewModel(
        key = T::class,
        factory = factory,
    )

private data class ViewModelHolder<T : ViewModel>(
    val viewModel: T,
) : InstanceKeeper.Instance {
    override fun onDestroy() {
        viewModel.onCleared()
    }
}
