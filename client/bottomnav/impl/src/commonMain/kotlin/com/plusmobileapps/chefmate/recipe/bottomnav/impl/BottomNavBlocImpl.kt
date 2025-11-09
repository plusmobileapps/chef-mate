package com.plusmobileapps.chefmate.recipe.bottomnav.impl

import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Child.GroceryList
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Child.RecipeList
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Child.Settings
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Output.OpenGrocery
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc
import com.plusmobileapps.chefmate.settings.SettingsBloc
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.StateFlow
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
    private val recipeList: RecipeListBloc.Factory,
    private val settings: SettingsBloc.Factory,
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
        observeRouter()
    }

    override val state: StateFlow<BottomNavBloc.Model> =
        viewModel.state.mapState {
            BottomNavBloc.Model(
                selectedTab = it.selectedTab,
                tabs = it.tabs,
            )
        }

    override val content: Value<ChildStack<*, BottomNavBloc.Child>> = stack

    override fun onBackClicked() {
        navigation.pop()
    }

    override fun onTabSelected(tab: BottomNavBloc.Tab) {
        val configuration =
            when (tab) {
                BottomNavBloc.Tab.RECIPES -> Configuration.Recipe
                BottomNavBloc.Tab.GROCERIES -> Configuration.Grocery
                BottomNavBloc.Tab.SETTINGS -> Configuration.Settings
            }
        navigation.bringToFront(configuration)
        viewModel.selectTab(tab)
    }

    private fun createChild(
        configuration: Configuration,
        context: BlocContext,
    ): BottomNavBloc.Child =
        when (configuration) {
            Configuration.Recipe -> {
                RecipeList(
                    recipeList.create(
                        context = context,
                        output = ::handleRecipeListOutput,
                    ),
                )
            }

            Configuration.Grocery -> {
                val bloc =
                    groceryList.create(
                        context = context,
                        output = ::handleGroceryListOutput,
                    )
                GroceryList(bloc)
            }

            Configuration.Settings -> {
                val bloc =
                    settings.create(
                        context = context,
                        output = ::handleSettingsOutput,
                    )
                Settings(bloc)
            }
        }

    private fun handleRecipeListOutput(output: RecipeListBloc.Output) {
        when (output) {
            RecipeListBloc.Output.AddNewRecipe -> {
                this.output.onNext(BottomNavBloc.Output.AddNewRecipe)
            }
            is RecipeListBloc.Output.OpenRecipe -> {
                this.output.onNext(BottomNavBloc.Output.OpenRecipe(output.recipeId))
            }
        }
    }

    private fun handleGroceryListOutput(output: GroceryListBloc.Output) {
        when (output) {
            is GroceryListBloc.Output.OpenDetail -> {
                this.output.onNext(OpenGrocery(output.id))
            }
        }
    }

    private fun handleSettingsOutput(output: SettingsBloc.Output) {
        TODO()
    }

    private fun observeRouter() {
        var cancellation: Cancellation? = null
        lifecycle.doOnResume {
            cancellation =
                stack.subscribe { value ->
                    when (value.active.instance) {
                        is BottomNavBloc.Child.GroceryList -> BottomNavBloc.Tab.GROCERIES
                        is BottomNavBloc.Child.RecipeList -> BottomNavBloc.Tab.RECIPES
                        is Settings -> BottomNavBloc.Tab.SETTINGS
                    }.let(viewModel::selectTab)
                }
        }
        lifecycle.doOnPause {
            cancellation?.cancel()
            cancellation = null
        }
    }

    @Serializable
    private sealed class Configuration {
        @Serializable
        data object Recipe : Configuration()

        @Serializable
        data object Grocery : Configuration()

        @Serializable
        data object Settings : Configuration()
    }
}
