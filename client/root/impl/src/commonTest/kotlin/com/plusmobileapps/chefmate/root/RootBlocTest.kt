package com.plusmobileapps.chefmate.root

import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.grocery.list.GroceryListBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.suspendTest
import dev.mokkery.mock
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf

class RootBlocTest :
    FunSpec({
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

        suspendTest("When initialized, Then list is shown") {
            rootBloc.instance() should instanceOf<RootBloc.Child.GroceryList>()
            rootBloc.state.value.backStack.size shouldBe 0
        }

        suspendTest("Given list, When list outputs open detail, Then detail is shown") {
            val itemId = 123L
            listOutput.onNext(GroceryListBloc.Output.OpenDetail(itemId))
            rootBloc.instance() should instanceOf<RootBloc.Child.GroceryDetail>()
            rootBloc.state.value.backStack.size shouldBe 1
            detailId shouldBe itemId
        }

        suspendTest("Given detail, When detail outputs close, Then list is shown") {
            listOutput.onNext(GroceryListBloc.Output.OpenDetail(123L))
            rootBloc.instance() should instanceOf<RootBloc.Child.GroceryDetail>()
            detailOutput.onNext(GroceryDetailBloc.Output.Finished)
            rootBloc.instance() should instanceOf<RootBloc.Child.GroceryList>()
        }

        this.isolationMode = IsolationMode.InstancePerRoot
    })
