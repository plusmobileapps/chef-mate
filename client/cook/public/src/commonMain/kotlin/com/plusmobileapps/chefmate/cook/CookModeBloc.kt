package com.plusmobileapps.chefmate.cook

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface CookModeBloc : BackClickBloc, ComposeScreen {
    val state: StateFlow<Model>

    /**
     * What's Cooking sub-bloc — kept alive for the lifetime of Cook Mode so the inline mobile sheet
     * and the tablet modal share its state (selection, select-mode toggle, etc.).
     */
    val whatsCookingBloc: WhatsCookingBloc

    fun onCloseClicked()

    fun onRecipeChipClicked(recipeId: Long)

    fun onLayoutToggled()

    fun onKeepScreenOnToggled()

    enum class LayoutMode {
        Stacked,
        Split,
    }

    data class Model(
        val isLoading: Boolean = true,
        val activeRecipe: Recipe? = null,
        val activeSessions: ImmutableList<Chip> = persistentListOf(),
        val layoutMode: LayoutMode = LayoutMode.Split,
        val keepScreenOn: Boolean = true,
    ) {
        data class Chip(val recipeId: Long, val title: String, val isActive: Boolean)
    }

    sealed class Output {
        data object Finished : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            initialRecipeId: Long,
            output: Consumer<Output>,
        ): CookModeBloc
    }
}
