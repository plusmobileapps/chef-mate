package com.plusmobileapps.chefmate.watch

/**
 * Swift-facing DTOs exposed by [WatchGroceryController]. Deliberately built from primitive types
 * only so the watchOS SwiftUI app needs nothing beyond `import WatchShared` — no cross-module
 * framework exports of the shared Kotlin models are required.
 */
data class WatchGroceryList(val id: Long, val name: String, val isShared: Boolean)

data class WatchGroceryItem(
    val id: Long,
    val name: String,
    val quantity: String?,
    /** [com.plusmobileapps.chefmate.grocery.data.GroceryCategory] name, e.g. "PRODUCE". */
    val category: String,
    val isChecked: Boolean,
)

/** Handle returned by the `observe*` calls so Swift can stop collecting the underlying flow. */
class WatchCancellable internal constructor(private val onCancel: () -> Unit) {
    fun cancel() {
        onCancel()
    }
}
