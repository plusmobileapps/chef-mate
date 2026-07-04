package com.plusmobileapps.chefmate.cook

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.cook.ui.CookModeScreen
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

    /** Clears every recipe from cook mode and finishes the session. */
    fun onFinishClicked()

    fun onRecipeChipClicked(recipeId: Long)

    fun onLayoutToggled()

    fun onKeepScreenOnToggled()

    /** Marks the first-run coach mark with [id] as seen so it never shows again. */
    fun onCoachMarkDismissed(id: String)

    @Composable
    override fun Content(modifier: Modifier) {
        CookModeScreen(bloc = this, modifier = modifier)
    }

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
        /** Id of the first-run coach mark currently allowed to show, or null when none. */
        val activeCoachMark: String? = null,
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
