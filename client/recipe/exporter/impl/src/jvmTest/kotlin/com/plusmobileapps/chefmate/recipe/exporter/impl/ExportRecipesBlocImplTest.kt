@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.exporter.impl

import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class ExportRecipesBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<ExportRecipesBloc.Output>()
    private val seeded =
        MutableStateFlow(listOf(recipe(1L, "Garlic Noodles"), recipe(2L, "Lentil Curry")))
    private val repository = FakeRecipeRepository(seeded)
    private val dispatcher = UnconfinedTestDispatcher()

    private fun createBloc(
        props: ExportRecipesBloc.Props = ExportRecipesBloc.Props.All
    ): ExportRecipesBlocImpl =
        ExportRecipesBlocImpl(
            context = context,
            props = props,
            output = output,
            viewModelFactory = { p ->
                ExportRecipesViewModel(
                    props = p,
                    mainContext = dispatcher,
                    ioContext = dispatcher,
                    repository = repository,
                )
            },
        )

    @Test
    fun When_back_from_review_stage_Then_emits_Back() {
        val bloc = createBloc()
        bloc.onBack()
        output.lastValue shouldBe ExportRecipesBloc.Output.Back
    }

    @Test
    fun When_back_from_done_stage_Then_emits_Finished() {
        val bloc = createBloc()
        bloc.onExportClicked()
        bloc.onSaveCompleted(saved = true)

        bloc.onBack()

        output.lastValue shouldBe ExportRecipesBloc.Output.Finished
    }

    @Test
    fun When_back_from_error_stage_Then_emits_Back() {
        val bloc = createBloc()
        bloc.onExportClicked()
        bloc.onSaveCompleted(saved = false) // cancel → Error stage

        bloc.onBack()

        output.lastValue shouldBe ExportRecipesBloc.Output.Back
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
