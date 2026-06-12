package com.plusmobileapps.chefmate

/**
 * Returns the result of [block] when [condition] is `true`, or `null` otherwise.
 *
 * Handy for conditionally supplying an optional value — such as a nullable trailing-content
 * composable — without the noisy `if (condition) { … } else null` idiom:
 * ```kotlin
 * trailingContent = letIfTrue(canRemove) {
 *     { IconButton(onClick = onRemove) { /* … */ } }
 * }
 * ```
 */
inline fun <T> letIfTrue(condition: Boolean, block: () -> T): T? = if (condition) block() else null
