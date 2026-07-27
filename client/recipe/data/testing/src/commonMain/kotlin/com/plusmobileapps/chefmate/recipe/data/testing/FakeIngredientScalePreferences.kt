package com.plusmobileapps.chefmate.recipe.data.testing

import com.plusmobileapps.chefmate.recipe.data.DEFAULT_INGREDIENT_SCALE
import com.plusmobileapps.chefmate.recipe.data.IngredientScalePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory [IngredientScalePreferences] for tests — same per-id hot-flow semantics, no storage.
 */
class FakeIngredientScalePreferences(initial: Map<Long, Double> = emptyMap()) :
    IngredientScalePreferences {

    private val flows =
        initial.mapValuesTo(mutableMapOf()) { (_, scale) -> MutableStateFlow(scale) }

    override fun scaleFor(recipeId: Long): StateFlow<Double> = flowFor(recipeId).asStateFlow()

    override fun setScale(recipeId: Long, scale: Double) {
        flowFor(recipeId).value = scale
    }

    private fun flowFor(recipeId: Long): MutableStateFlow<Double> =
        flows.getOrPut(recipeId) { MutableStateFlow(DEFAULT_INGREDIENT_SCALE) }
}
