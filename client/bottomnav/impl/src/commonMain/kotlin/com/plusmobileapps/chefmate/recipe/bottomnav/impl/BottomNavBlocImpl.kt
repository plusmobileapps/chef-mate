package com.plusmobileapps.chefmate.recipe.bottomnav.impl

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.grocery.list.GroceryListBloc
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Output.OpenGrocery
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(AppScope::class, assistedFactory = BottomNavBloc.Factory::class)
class BottomNavBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<BottomNavBloc.Output>,
    viewModelFactory: () -> BottomNavViewModel,
    private val groceryList: GroceryListBloc.Factory,
) : BottomNavBloc,
    BlocContext by context {
    private val scope = createScope()

    private val viewModel: BottomNavViewModel = instanceKeeper.getViewModel { viewModelFactory() }

    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialStack = {
                listOf(Configuration.Recipe)
            },
            handleBackButton = true,
            key = "BottomNavRouter",
            childFactory = ::createChild,
        )

    init {
        scope.launch {
            viewModel.state.collect {
                val configuration =
                    when (it.selectedTab) {
                        BottomNavBloc.Tab.RECIPES -> Configuration.Recipe
                        BottomNavBloc.Tab.GROCERIES -> Configuration.Grocery
                    }
                navigation.bringToFront(configuration)
            }
        }
    }

    override val state: StateFlow<BottomNavBloc.Model> =
        viewModel.state.mapState {
            BottomNavBloc.Model(
                selectedTab = it.selectedTab,
                tabs = it.tabs,
            )
        }

    override val content: Value<ChildStack<*, BottomNavBloc.Child>> = stack

    override fun onTabSelected(tab: BottomNavBloc.Tab) {
        viewModel.selectTab(tab)
    }

    private fun createChild(
        configuration: Configuration,
        context: BlocContext,
    ): BottomNavBloc.Child =
        when (configuration) {
            Configuration.Recipe -> {
                BottomNavBloc.Child.RecipeList
            }

            Configuration.Grocery -> {
                val bloc =
                    groceryList.create(
                        context = context,
                        output = ::handleGroceryListOutput,
                    )
                BottomNavBloc.Child.GroceryList(bloc)
            }
        }

    private fun handleGroceryListOutput(output: GroceryListBloc.Output) {
        when (output) {
            is GroceryListBloc.Output.OpenDetail -> {
                this.output.onNext(OpenGrocery(output.id))
            }
        }
    }

    @Serializable
    private sealed class Configuration {
        @Serializable
        data object Recipe : Configuration()

        @Serializable
        data object Grocery : Configuration()
    }
}
