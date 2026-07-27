@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.cook.impl

import com.plusmobileapps.chefmate.cook.data.CookingSessionRepository
import com.plusmobileapps.chefmate.di.CoachMarkController
import com.plusmobileapps.chefmate.di.CoachMarkId
import com.plusmobileapps.chefmate.di.KeepScreenOnRepository
import com.plusmobileapps.chefmate.featureflag.FeatureFlagRegistry
import com.plusmobileapps.chefmate.featureflag.testing.FakeFeatureFlags
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.testing.FakeIngredientScalePreferences
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.russhwolf.settings.MapSettings
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class CookModeViewModelTest {

    private val recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val recipeRepository = FakeRecipeRepository(recipes)
    private val recipeIds = MutableStateFlow<List<Long>>(emptyList())
    private val sessionRepository: CookingSessionRepository = mock {
        every { observeRecipeIds() } returns recipeIds
        everySuspend { start(any()) } returns Unit
        everySuspend { markSelected(any()) } returns Unit
        everySuspend { stopAll() } returns Unit
    }
    private val scalePreferences = FakeIngredientScalePreferences()

    private fun createViewModel(
        coachMarkController: CoachMarkController = CoachMarkController(MapSettings()),
        featureFlags: FakeFeatureFlags = FakeFeatureFlags(),
    ) =
        CookModeViewModel(
            initialRecipeId = 1L,
            mainContext = UnconfinedTestDispatcher(),
            recipeRepository = recipeRepository,
            sessionRepository = sessionRepository,
            settings = MapSettings(),
            scalePreferences = scalePreferences,
            keepScreenOnRepository = KeepScreenOnRepository(MapSettings()),
            coachMarkController = coachMarkController,
            featureFlags = featureFlags,
        )

    @Test
    fun When_screen_opens_Then_first_coach_mark_active() {
        val vm = createViewModel()
        vm.state.value.activeCoachMark shouldBe CoachMarkId.COOK_MODE_KEEP_SCREEN_ON
    }

    @Test
    fun When_active_coach_mark_dismissed_Then_persisted_and_next_shown() {
        val controller = CoachMarkController(MapSettings())
        val vm = createViewModel(coachMarkController = controller)

        vm.dismissCoachMark(CoachMarkId.COOK_MODE_KEEP_SCREEN_ON)

        controller.hasSeen(CoachMarkId.COOK_MODE_KEEP_SCREEN_ON) shouldBe true
        vm.state.value.activeCoachMark shouldBe CoachMarkId.COOK_MODE_LAYOUT
    }

    @Test
    fun When_ai_chat_flag_off_Then_showAiChat_is_false() {
        val vm = createViewModel()
        vm.state.value.showAiChat shouldBe false
    }

    @Test
    fun When_ai_chat_flag_on_Then_showAiChat_is_true() {
        val vm =
            createViewModel(
                featureFlags = FakeFeatureFlags(mapOf(FeatureFlagRegistry.AiChat to true))
            )
        vm.state.value.showAiChat shouldBe true
    }

    @Test
    fun When_finish_cooking_Then_all_sessions_cleared_and_callback_run() {
        val vm = createViewModel()
        var finished = false

        vm.finishCooking { finished = true }

        verifySuspend { sessionRepository.stopAll() }
        finished shouldBe true
    }

    @Test
    fun When_recipe_has_saved_scale_Then_state_reflects_it() {
        scalePreferences.setScale(1L, 2.0)
        recipes.value = listOf(Recipe.Sample.copy(id = 1L))
        recipeIds.value = listOf(1L)

        val vm = createViewModel()

        vm.state.value.ingredientScale shouldBe 2.0
    }

    @Test
    fun When_setScale_called_Then_persisted_for_active_recipe_and_state_updates() {
        recipes.value = listOf(Recipe.Sample.copy(id = 1L))
        recipeIds.value = listOf(1L)
        val vm = createViewModel()

        vm.setScale(3.0)

        scalePreferences.scaleFor(1L).value shouldBe 3.0
        vm.state.value.ingredientScale shouldBe 3.0
    }

    @Test
    fun When_no_active_recipe_Then_scale_defaults_to_one() {
        val vm = createViewModel()
        vm.state.value.ingredientScale shouldBe 1.0
    }

    @Test
    fun When_inactive_coach_mark_dismissed_Then_ignored() {
        val controller = CoachMarkController(MapSettings())
        val vm = createViewModel(coachMarkController = controller)

        vm.dismissCoachMark(CoachMarkId.COOK_MODE_LAYOUT)

        controller.hasSeen(CoachMarkId.COOK_MODE_LAYOUT) shouldBe false
        vm.state.value.activeCoachMark shouldBe CoachMarkId.COOK_MODE_KEEP_SCREEN_ON
    }
}
