@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.cook

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.plusmobileapps.chefmate.cook.robots.WhatsCookingRobot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Per-screen multiplatform Compose UI test for [WhatsCookingScreen]. Runs on:
 * - JVM via `:client:cook:public:jvmTest`
 * - iOS simulator via `:client:cook:public:iosSimulatorArm64Test`
 * - Android emulator via `:client:cook:public:connectedDebugAndroidTest` (because
 *   `enableComposeUiTest = true` routes commonTest to the instrumented test source set tree)
 */
class WhatsCookingScreenTest {

    @Test
    fun rendersAllRecipes() = runComposeUiTest {
        val bloc =
            FakeWhatsCookingBloc(
                recipes =
                    listOf(
                        WhatsCookingBloc.Model.Item(1L, "Pasta Carbonara", null),
                        WhatsCookingBloc.Model.Item(2L, "Caesar Salad", null),
                    )
            )
        setContent { WhatsCookingScreen(bloc = bloc) }

        WhatsCookingRobot(this)
            .assertRecipeListed("Pasta Carbonara")
            .assertRecipeListed("Caesar Salad")
    }

    @Test
    fun rendersEmptyMessageWhenNoRecipes() = runComposeUiTest {
        val bloc = FakeWhatsCookingBloc(recipes = emptyList())
        setContent { WhatsCookingScreen(bloc = bloc) }

        onNodeWithText("No recipes are currently cooking.").assertIsDisplayed()
    }

    @Test
    fun tappingRecipeRowInvokesOnRecipeClicked() = runComposeUiTest {
        val bloc =
            FakeWhatsCookingBloc(recipes = listOf(WhatsCookingBloc.Model.Item(42L, "Tacos", null)))
        setContent { WhatsCookingScreen(bloc = bloc) }

        onNodeWithTag(WhatsCookingTestTags.recipeRow(42L)).performClick()

        assertEquals(listOf(42L), bloc.recipeClicks)
    }

    @Test
    fun togglingSelectModeFlipsState() = runComposeUiTest {
        val bloc =
            FakeWhatsCookingBloc(recipes = listOf(WhatsCookingBloc.Model.Item(1L, "Pasta", null)))
        setContent { WhatsCookingScreen(bloc = bloc) }

        assertEquals(false, bloc.state.value.isSelectMode)

        WhatsCookingRobot(this).toggleSelectMode()

        assertEquals(true, bloc.state.value.isSelectMode)
    }
}

private class FakeWhatsCookingBloc(recipes: List<WhatsCookingBloc.Model.Item>) : WhatsCookingBloc {
    override val state = MutableStateFlow(WhatsCookingBloc.Model(recipes = recipes))

    val recipeClicks = mutableListOf<Long>()

    override fun onRecipeClicked(recipeId: Long) {
        recipeClicks += recipeId
    }

    override fun onSelectModeToggled() {
        state.value = state.value.copy(isSelectMode = !state.value.isSelectMode)
    }

    override fun onSelectionToggled(recipeId: Long) {
        val current = state.value.selectedRecipeIds
        val next = if (recipeId in current) current - recipeId else current + recipeId
        state.value = state.value.copy(selectedRecipeIds = next)
    }

    override fun onDeleteSelectedClicked() = Unit

    override fun onCloseClicked() = Unit
}
