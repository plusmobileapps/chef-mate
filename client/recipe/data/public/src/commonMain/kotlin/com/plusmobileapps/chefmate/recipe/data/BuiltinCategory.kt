package com.plusmobileapps.chefmate.recipe.data

/**
 * Registry of built-in category presets shipped with the app. The [id] is the stable string
 * persisted on a [Category] row's `builtinId` field locally and remotely; ordinals are not used.
 * Reordering or renaming an entry would orphan previously-attached recipes — add a new ID instead.
 *
 * Built-ins live as code constants and are *materialized* into a `Category` DB row the first time a
 * user attaches one to a recipe. Display names come from `strings.xml` (keyed by [id]), so renaming
 * a preset for a different locale is just a translation change.
 */
enum class BuiltinCategory(val id: String) {
    BREAKFAST("breakfast"),
    LUNCH("lunch"),
    DINNER("dinner"),
    APPETIZER("appetizer"),
    SIDE("side"),
    DESSERT("dessert"),
    SNACK("snack"),
    DRINK("drink"),
    OTHER("other");

    companion object {
        fun fromId(id: String?): BuiltinCategory? = id?.let { value ->
            entries.firstOrNull { it.id == value }
        }
    }
}
