package com.plusmobileapps.chefmate.recipe.data.impl

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.data.DEFAULT_INGREDIENT_SCALE
import com.plusmobileapps.chefmate.recipe.data.IngredientScalePreferences
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class IngredientScalePreferencesImpl(private val settings: Settings) : IngredientScalePreferences {

    // One hot flow per recipe id, created lazily from the persisted value and shared by every
    // screen
    // that scales that recipe so they update together. Accessed from BLoC view models on the main
    // dispatcher, so a plain map is safe here.
    private val flows = mutableMapOf<Long, MutableStateFlow<Double>>()

    override fun scaleFor(recipeId: Long): StateFlow<Double> = flowFor(recipeId).asStateFlow()

    override fun setScale(recipeId: Long, scale: Double) {
        val key = key(recipeId)
        // 1× is the author's own amounts — clear the key instead of storing the default so we don't
        // accumulate an entry for every recipe the user merely opened.
        if (scale == DEFAULT_INGREDIENT_SCALE) {
            settings.remove(key)
        } else {
            settings.putDouble(key, scale)
        }
        flowFor(recipeId).value = scale
    }

    private fun flowFor(recipeId: Long): MutableStateFlow<Double> =
        flows.getOrPut(recipeId) {
            MutableStateFlow(settings.getDouble(key(recipeId), DEFAULT_INGREDIENT_SCALE))
        }

    private fun key(recipeId: Long): String = "$KEY_PREFIX$recipeId"

    private companion object {
        const val KEY_PREFIX = "recipe_scale_"
    }
}
