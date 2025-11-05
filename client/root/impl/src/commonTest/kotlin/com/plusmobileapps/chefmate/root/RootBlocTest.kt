@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.root

import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import dev.mokkery.mock
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import kotlin.test.Test

class RootBlocTest {
    val context = TestBlocContext.create()
    var bottomNavOutput: Consumer<BottomNavBloc.Output> = Consumer {}
    var detailOutput: Consumer<GroceryDetailBloc.Output> = Consumer {}
    var recipeOutput: Consumer<RecipeRootBloc.Output> = Consumer {}
    var recipeProps: RecipeRootBloc.Props? = null
    var groceryDetailId: Long? = null

    val rootBloc =
        RootBlocImpl(
            context = context,
            bottomNav = { context, output ->
                bottomNavOutput = output
                mock()
            },
            recipeRoot = { context, props, output ->
                recipeOutput = output
                recipeProps = props
                mock()
            },
            groceryDetail = { _, id, output ->
                groceryDetailId = id
                detailOutput = output
                mock()
            },
        )

    fun RootBloc.instance(): RootBloc.Child = state.value.active.instance

    @Test
    fun When_initialized_Then_bottom_nav_is_shown() {
        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavigation>()
        rootBloc.state.value.backStack.size shouldBe 0
    }

    @Test
    fun When_bottom_nav_outputs_open_detail_Then_detail_is_shown() {
        val itemId = 123L
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenGrocery(itemId))
        rootBloc.instance() should instanceOf<RootBloc.Child.GroceryDetail>()
        rootBloc.state.value.backStack.size shouldBe 1
        groceryDetailId shouldBe itemId
    }

    @Test
    fun Given_detail_When_detail_outputs_close_Then_bottom_nav_is_shown() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenGrocery(123L))
        rootBloc.instance() should instanceOf<RootBloc.Child.GroceryDetail>()
        detailOutput.onNext(GroceryDetailBloc.Output.Finished)
        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavigation>()
    }

    @Test
    fun When_bottom_nav_outputs_create_recipe_Then_recipe_root_shown_with_create_props() {
        bottomNavOutput.onNext(BottomNavBloc.Output.AddNewRecipe)
        rootBloc.instance() should instanceOf<RootBloc.Child.RecipeRoot>()
        rootBloc.state.value.backStack.size shouldBe 1
        recipeProps shouldBe RecipeRootBloc.Props.Create

        recipeOutput.onNext(RecipeRootBloc.Output.Finished)
        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavigation>()
    }

    @Test
    fun When_bottom_nav_outputs_open_recipe_Then_recipe_root_shown_with_detail_props() {
        bottomNavOutput.onNext(BottomNavBloc.Output.OpenRecipe(123L))
        rootBloc.instance() should instanceOf<RootBloc.Child.RecipeRoot>()
        rootBloc.state.value.backStack.size shouldBe 1
        recipeProps shouldBe RecipeRootBloc.Props.Detail(123L)

        recipeOutput.onNext(RecipeRootBloc.Output.Finished)
        rootBloc.instance() should instanceOf<RootBloc.Child.BottomNavigation>()
    }
}
