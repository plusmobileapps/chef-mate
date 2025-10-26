package com.plusmobileapps.chefmate.recipe.core.impl.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc
import com.plusmobileapps.chefmate.recipe.core.edit.EditRecipeBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = RecipeRootBloc.Factory::class,
)
class RecipeRootBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val props: RecipeRootBloc.Props,
    @Assisted private val output: Consumer<RecipeRootBloc.Output>,
    private val detailBloc: RecipeDetailBloc.Factory,
    private val editBloc: EditRecipeBloc.Factory,
) : RecipeRootBloc,
    BlocContext by context {

    private val navigation = StackNavigation<Configuration>()
    private val stack = childStack(
        source = navigation,
        serializer = Configuration.serializer(),
        initialStack = {
            when (props) {
                is RecipeRootBloc.Props.Detail -> listOf(
                    Configuration.Detail(recipeId = props.recipeId),
                )
                is RecipeRootBloc.Props.Create -> listOf(
                    Configuration.Edit(recipeId = null),
                )
            }
        },
        handleBackButton = true,
        key = "RecipeRootRouter",
        childFactory = ::createChild,
    )

    override val routerState: Value<ChildStack<*, RecipeRootBloc.Child>> = stack

    private fun createChild(
        config: Configuration,
        context: BlocContext,
    ): RecipeRootBloc.Child =
        when (config) {
            is Configuration.Detail ->
                RecipeRootBloc.Child.Detail(
                    bloc = detailBloc.create(
                        context = context,
                        recipeId = config.recipeId,
                        output = ::handleDetailOutput,
                    ),
                )
            is Configuration.Edit ->
                RecipeRootBloc.Child.Edit(
                    bloc = editBloc.create(
                        context = context,
                        recipeId = config.recipeId,
                        output = ::handleEditOutput,
                    ),
                )
        }

    private fun handleEditOutput(output: EditRecipeBloc.Output) {
        when (output) {
            EditRecipeBloc.Output.Cancelled -> {
                if (props is RecipeRootBloc.Props.Create) {
                    this.output.onNext(RecipeRootBloc.Output.Finished)
                } else {
                    navigation.pop()
                }
            }
            is EditRecipeBloc.Output.Finished -> {
                navigation.navigate {
                    listOf(Configuration.Detail(output.recipeId))
                }
            }
        }
    }

    private fun handleDetailOutput(output: RecipeDetailBloc.Output) {
        when (output) {
            RecipeDetailBloc.Output.Finished -> {
                this.output.onNext(RecipeRootBloc.Output.Finished)
            }
            is RecipeDetailBloc.Output.EditRecipe -> {
                navigation.bringToFront(
                    Configuration.Edit(recipeId = output.recipeId)
                )
            }
        }
    }

    @Serializable
    sealed class Configuration {
        data class Detail(
            val recipeId: Long,
        ) : Configuration()

        data class Edit(
            val recipeId: Long?,
        ) : Configuration()
    }
}
