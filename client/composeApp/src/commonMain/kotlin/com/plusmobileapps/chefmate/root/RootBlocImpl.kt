package com.plusmobileapps.chefmate.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.grocerylist.list.GroceryListBloc
import kotlinx.serialization.Serializable

class RootBlocImpl(
    context: BlocContext,
    private val groceryListBloc: (BlocContext) -> GroceryListBloc,
) : RootBloc, BlocContext by context {

    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialStack = {
                listOf(Configuration.GroceryList)
            },
            handleBackButton = true,
            key = "RootRouter",
            childFactory = ::createChild,
        )

    override val state: Value<ChildStack<*, RootBloc.Child>> = stack

    private fun createChild(config: Configuration, context: BlocContext): RootBloc.Child =
        when (config) {
            Configuration.GroceryList -> RootBloc.Child.GroceryList(
                bloc = groceryListBloc(context)
            )
        }

    @Serializable
    private sealed class Configuration {

        @Serializable
        data object GroceryList : Configuration()
    }
}