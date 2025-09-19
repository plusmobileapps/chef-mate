@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.root

import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.grocery.list.GroceryListBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import dev.mokkery.mock
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import kotlin.test.Test

class RootBlocTest {
    val context = TestBlocContext.create()
    var listOutput: Consumer<GroceryListBloc.Output> = Consumer {}
    var detailOutput: Consumer<GroceryDetailBloc.Output> = Consumer {}
    var detailId: Long? = null

    val rootBloc =
        RootBlocImpl(
            context = context,
            groceryListBloc = { _, output ->
                listOutput = output
                mock()
            },
            groceryDetail = { _, id, output ->
                detailId = id
                detailOutput = output
                mock()
            },
        )

    fun RootBloc.instance(): RootBloc.Child = state.value.active.instance

    @Test
    fun When_initialized_Then_list_is_shown() {
        rootBloc.instance() should instanceOf<RootBloc.Child.GroceryList>()
        rootBloc.state.value.backStack.size shouldBe 0
    }

    @Test
    fun Given_list_When_list_outputs_open_detail_Then_detail_is_shown() {
        val itemId = 123L
        listOutput.onNext(GroceryListBloc.Output.OpenDetail(itemId))
        rootBloc.instance() should instanceOf<RootBloc.Child.GroceryDetail>()
        rootBloc.state.value.backStack.size shouldBe 1
        detailId shouldBe itemId
    }

    @Test
    fun Given_detail_When_detail_outputs_close_Then_list_is_shown() {
        listOutput.onNext(GroceryListBloc.Output.OpenDetail(123L))
        rootBloc.instance() should instanceOf<RootBloc.Child.GroceryDetail>()
        detailOutput.onNext(GroceryDetailBloc.Output.Finished)
        rootBloc.instance() should instanceOf<RootBloc.Child.GroceryList>()
    }
}
