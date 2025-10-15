@file:OptIn(DelicateDecomposeApi::class)

package com.plusmobileapps.chefmate.root

import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.grocery.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.grocery.list.GroceryListBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = RootBloc.Factory::class,
)
class RootBlocImpl(
    @Assisted context: BlocContext,
    private val bottomNav: BottomNavBloc.Factory,
    private val groceryDetail: GroceryDetailBloc.Factory,
) : RootBloc,
    BlocContext by context {
    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialStack = {
                listOf(Configuration.BottomNavigation)
            },
            handleBackButton = true,
            key = "RootRouter",
            childFactory = ::createChild,
        )

    override val state: Value<ChildStack<*, RootBloc.Child>> = stack

    private fun createChild(
        config: Configuration,
        context: BlocContext,
    ): RootBloc.Child =
        when (config) {
            Configuration.BottomNavigation ->
                RootBloc.Child.BottomNavigation(
                    bottomNav.create(
                        context = context,
                        output = ::handleBottomNavOutput
                    )
                )

            is Configuration.GroceryDetail ->
                RootBloc.Child.GroceryDetail(
                    bloc =
                        groceryDetail.create(
                            context = context,
                            id = config.itemId,
                            output = ::onDetailOutput,
                        ),
                )
        }

    private fun handleBottomNavOutput(output: BottomNavBloc.Output) {
        when (output) {
            BottomNavBloc.Output.AddNewRecipe -> TODO()
            is BottomNavBloc.Output.OpenGrocery -> TODO()
            is BottomNavBloc.Output.OpenRecipe -> TODO()
        }
    }

    private fun onDetailOutput(output: GroceryDetailBloc.Output) {
        when (output) {
            GroceryDetailBloc.Output.Finished -> navigation.pop()
        }
    }

    @Serializable
    private sealed class Configuration {
        @Serializable
        data object BottomNavigation : Configuration()

        @Serializable
        data class GroceryDetail(
            val itemId: Long,
        ) : Configuration()
    }
}
