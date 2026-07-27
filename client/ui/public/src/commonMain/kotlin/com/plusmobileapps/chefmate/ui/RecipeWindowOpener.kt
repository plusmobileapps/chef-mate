package com.plusmobileapps.chefmate.ui

import androidx.compose.runtime.compositionLocalOf

/**
 * Opens a single recipe in its own detached OS window.
 *
 * Desktop only. The JVM app provides an implementation from its `application { }` block, which is
 * where the list of open windows lives; every other target leaves this `null`, and a `null` opener
 * is the signal for UI to leave the "open in new window" affordance out entirely rather than show a
 * dead menu item.
 *
 * This lives in the shared UI module rather than a recipe module because the provider
 * (`composeApp`'s desktop entry point) and the consumer (the recipe list) share no recipe module,
 * and it keeps company with the other app-wide composition locals here.
 */
fun interface RecipeWindowOpener {
    /** [title] seeds the new window's title bar so it is identifiable before the recipe loads. */
    fun open(recipeId: Long, title: String)
}

val LocalRecipeWindowOpener = compositionLocalOf<RecipeWindowOpener?> { null }
