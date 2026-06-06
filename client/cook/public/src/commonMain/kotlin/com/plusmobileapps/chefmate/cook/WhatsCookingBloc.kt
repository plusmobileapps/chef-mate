package com.plusmobileapps.chefmate.cook

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface WhatsCookingBloc {
    val state: StateFlow<Model>

    fun onRecipeClicked(recipeId: Long)

    fun onSelectModeToggled()

    fun onSelectionToggled(recipeId: Long)

    fun onDeleteSelectedClicked()

    fun onCloseClicked()

    data class Model(
        val recipes: ImmutableList<Item> = persistentListOf(),
        val isSelectMode: Boolean = false,
        val selectedRecipeIds: Set<Long> = emptySet(),
    ) {
        val hasSelection: Boolean
            get() = selectedRecipeIds.isNotEmpty()

        data class Item(val recipeId: Long, val title: String, val imageUrl: String?)
    }

    sealed class Output {
        data object Closed : Output()

        data class RecipeSelected(val recipeId: Long) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): WhatsCookingBloc
    }
}
