@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.exporter.impl

import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.plusmobileapps.chefmate.recipe.exporter.impl.ExportRecipesViewModel.Stage
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class ExportRecipesViewModelTest {

    private val seededRecipes =
        MutableStateFlow(
            listOf(
                recipe(id = 1L, title = "Garlic Noodles"),
                recipe(id = 2L, title = "Lentil Curry"),
            )
        )
    private val repository = FakeRecipeRepository(seededRecipes)
    private val dispatcher = UnconfinedTestDispatcher()

    private fun createViewModel() =
        ExportRecipesViewModel(
            mainContext = dispatcher,
            ioContext = dispatcher,
            repository = repository,
        )

    @Test
    fun When_recipes_loaded_Then_review_stage_lists_them_pre_selected() {
        val vm = createViewModel()

        val review = vm.state.value.stage.shouldBeInstanceOf<Stage.Review>()
        review.items.map { it.title } shouldBe listOf("Garlic Noodles", "Lentil Curry")
        review.items.all { it.selected } shouldBe true
    }

    @Test
    fun When_no_recipes_Then_empty_stage_shown() {
        seededRecipes.value = emptyList()
        val vm = createViewModel()
        vm.state.value.stage shouldBe Stage.Empty
    }

    @Test
    fun When_export_clicked_Then_pending_save_carries_archive_for_selected_recipes() {
        val vm = createViewModel()
        // Deselect Lentil Curry — only Garlic Noodles should be in the archive.
        vm.onRecipeToggled(id = "2")

        vm.onExportClicked()

        val pending = vm.state.value.pendingSave
        pending shouldNotBe null
        pending!!.fileName shouldBe "chef-mate-1-recipe.zip"
        val text = pending.archive.decodeToString()
        // Index points at Garlic Noodles only.
        text.contains("Garlic Noodles.html") shouldBe true
        text.contains("Lentil Curry") shouldBe false
    }

    @Test
    fun When_save_succeeds_Then_done_stage_shown_with_exported_count() {
        val vm = createViewModel()
        vm.onExportClicked()
        val initiallyPending = vm.state.value.pendingSave
        initiallyPending shouldNotBe null

        vm.onSaveCompleted(saved = true)

        val done = vm.state.value.stage.shouldBeInstanceOf<Stage.Done>()
        done.exportedCount shouldBe 2
        vm.state.value.pendingSave shouldBe null
    }

    @Test
    fun When_save_cancelled_Then_error_stage_shown() {
        val vm = createViewModel()
        vm.onExportClicked()

        vm.onSaveCompleted(saved = false)

        vm.state.value.stage.shouldBeInstanceOf<Stage.Error>()
        vm.state.value.pendingSave shouldBe null
    }

    @Test
    fun When_start_over_clicked_Then_reloads_recipes() {
        val vm = createViewModel()
        vm.onExportClicked()
        vm.onSaveCompleted(saved = false)
        vm.state.value.stage.shouldBeInstanceOf<Stage.Error>()

        vm.onStartOver()

        vm.state.value.stage.shouldBeInstanceOf<Stage.Review>()
    }

    private fun recipe(id: Long, title: String): Recipe =
        Recipe.Empty.copy(
            id = id,
            title = title,
            ingredients = "salt\npepper",
            directions = "Mix.\nServe.",
            createdAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST,
        )
}
