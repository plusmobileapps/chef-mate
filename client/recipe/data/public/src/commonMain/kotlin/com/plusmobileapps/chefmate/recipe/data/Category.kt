package com.plusmobileapps.chefmate.recipe.data

/**
 * A category that can be attached to a [Recipe]. May represent a built-in preset (when [builtinId]
 * matches a [BuiltinCategory.id]) or a user-created category (when [builtinId] is null). Built-ins
 * are materialized into a real DB row the first time the user attaches them — see
 * `CategoryRepository.materializeBuiltin`.
 *
 * For presets, [name] holds the locale-at-materialization fallback; the UI should render
 * `BuiltinCategory.fromId(builtinId)?.let { ... strings.xml lookup ... }` instead so the label
 * reacts to the user's current locale.
 */
data class Category(
    val id: Long,
    val name: String,
    val builtinId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
) {
    val isBuiltin: Boolean
        get() = builtinId != null
}
