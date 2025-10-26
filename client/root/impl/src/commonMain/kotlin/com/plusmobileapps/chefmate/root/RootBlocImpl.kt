@file:OptIn(DelicateDecomposeApi::class)

package com.plusmobileapps.chefmate.root

import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.grocery.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.chefmate.root.RootBloc.Child.BottomNavigation
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
    private val recipeRoot: RecipeRootBloc.Factory,
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
                BottomNavigation(
                    bottomNav.create(
                        context = context,
                        output = ::handleBottomNavOutput,
                    ),
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

            is Configuration.RecipeRoot ->
                RootBloc.Child.RecipeRoot(
                    bloc =
                        recipeRoot.create(
                            context = context,
                            props = config.props,
                            output = ::handleRecipeRootOutput,
                        ),
                )
        }

    private fun handleBottomNavOutput(output: BottomNavBloc.Output) {
        when (output) {
            BottomNavBloc.Output.AddNewRecipe -> {
                navigation.bringToFront(
                    Configuration.RecipeRoot(RecipeRootBloc.Props.Create),
                )
            }
            is BottomNavBloc.Output.OpenGrocery -> {
                navigation.bringToFront(Configuration.GroceryDetail(output.groceryId))
            }
            is BottomNavBloc.Output.OpenRecipe -> {
                navigation.bringToFront(
                    Configuration.RecipeRoot(
                        RecipeRootBloc.Props.Detail(output.recipeId),
                    ),
                )
            }
        }
    }

    private fun onDetailOutput(output: GroceryDetailBloc.Output) {
        when (output) {
            GroceryDetailBloc.Output.Finished -> navigation.pop()
        }
    }

    private fun handleRecipeRootOutput(output: RecipeRootBloc.Output) {
        when (output) {
            RecipeRootBloc.Output.Finished -> navigation.pop()
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

        @Serializable
        data class RecipeRoot(
            val props: RecipeRootBloc.Props,
        ) : Configuration()
    }
}
