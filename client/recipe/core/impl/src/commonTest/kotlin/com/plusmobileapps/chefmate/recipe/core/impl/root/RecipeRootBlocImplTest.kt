@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.recipe.core.impl.root

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc
import com.plusmobileapps.chefmate.recipe.core.edit.EditRecipeBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class RecipeRootBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<RecipeRootBloc.Output>()
    private var detailOutput: Consumer<RecipeDetailBloc.Output> = Consumer {}
    private var editOutput: Consumer<EditRecipeBloc.Output> = Consumer {}

    private fun createBloc(
        props: RecipeRootBloc.Props = RecipeRootBloc.Props.Detail(1L)
    ): RecipeRootBlocImpl =
        RecipeRootBlocImpl(
            context = context,
            props = props,
            output = output,
            detailBloc =
                object : RecipeDetailBloc.Factory {
                    override fun create(
                        context: BlocContext,
                        recipeId: Long,
                        output: Consumer<RecipeDetailBloc.Output>,
                    ): RecipeDetailBloc {
                        detailOutput = output
                        return mock()
                    }
                },
            editBloc =
                EditRecipeBloc.Factory { _, _, _, output ->
                    editOutput = output
                    mock()
                },
        )

    @Test
    fun When_created_with_detail_props_Then_detail_child_is_shown() {
        val bloc = createBloc(RecipeRootBloc.Props.Detail(42L))
        bloc.routerState.value.active.instance.shouldBeInstanceOf<RecipeRootBloc.Child.Detail>()
    }

    @Test
    fun When_created_with_create_props_Then_edit_child_is_shown() {
        val bloc = createBloc(RecipeRootBloc.Props.Create)
        bloc.routerState.value.active.instance.shouldBeInstanceOf<RecipeRootBloc.Child.Edit>()
    }

    @Test
    fun When_detail_outputs_finished_Then_root_outputs_finished() {
        createBloc(RecipeRootBloc.Props.Detail(1L))
        detailOutput.onNext(RecipeDetailBloc.Output.Finished)
        output.lastValue shouldBe RecipeRootBloc.Output.Finished
    }

    @Test
    fun When_detail_outputs_edit_recipe_Then_edit_child_is_shown() {
        val bloc = createBloc(RecipeRootBloc.Props.Detail(1L))
        detailOutput.onNext(RecipeDetailBloc.Output.EditRecipe(1L))
        bloc.routerState.value.active.instance.shouldBeInstanceOf<RecipeRootBloc.Child.Edit>()
    }

    @Test
    fun When_detail_outputs_open_url_Then_root_outputs_open_url() {
        createBloc(RecipeRootBloc.Props.Detail(1L))
        detailOutput.onNext(RecipeDetailBloc.Output.OpenUrl("https://example.com/recipe"))
        output.lastValue shouldBe RecipeRootBloc.Output.OpenUrl("https://example.com/recipe")
    }

    @Test
    fun When_edit_outputs_cancelled_from_create_Then_root_outputs_finished() {
        createBloc(RecipeRootBloc.Props.Create)
        editOutput.onNext(EditRecipeBloc.Output.Cancelled)
        output.lastValue shouldBe RecipeRootBloc.Output.Finished
    }

    @Test
    fun When_edit_outputs_finished_Then_detail_child_is_shown() {
        val bloc = createBloc(RecipeRootBloc.Props.Detail(1L))
        detailOutput.onNext(RecipeDetailBloc.Output.EditRecipe(1L))
        editOutput.onNext(EditRecipeBloc.Output.Finished(1L))
        bloc.routerState.value.active.instance.shouldBeInstanceOf<RecipeRootBloc.Child.Detail>()
    }
}
