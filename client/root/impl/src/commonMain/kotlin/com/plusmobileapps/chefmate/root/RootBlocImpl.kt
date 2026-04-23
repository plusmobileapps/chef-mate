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
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.browser.BrowserBloc
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc.Props.Detail
import com.plusmobileapps.chefmate.root.RootBloc.Child.BottomNavigation
import com.plusmobileapps.chefmate.root.RootBlocImpl.Configuration.GroceryDetail
import com.plusmobileapps.chefmate.root.RootBlocImpl.Configuration.RecipeRoot
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.serialization.Serializable

@AssistedInject
class RootBlocImpl(
    @Assisted context: BlocContext,
    private val bottomNav: BottomNavBloc.Factory,
    private val browserBlocFactory: BrowserBloc.Factory,
    private val groceryDetail: GroceryDetailBloc.Factory,
    private val recipeRoot: RecipeRootBloc.Factory,
    private val authentication: AuthenticationBloc.Factory,
) : RootBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(context: BlocContext): RootBlocImpl
    }

    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialStack = { listOf(Configuration.BottomNavigation) },
            handleBackButton = true,
            key = "RootRouter",
            childFactory = ::createChild,
        )

    override val state: Value<ChildStack<*, RootBloc.Child>> = stack

    override fun onBackClicked() {
        navigation.pop()
    }

    private fun createChild(config: Configuration, context: BlocContext): RootBloc.Child =
        when (config) {
            Configuration.BottomNavigation ->
                BottomNavigation(
                    bottomNav.create(context = context, output = ::handleBottomNavOutput)
                )

            is Configuration.GroceryDetail ->
                RootBloc.Child.GroceryDetail(
                    bloc =
                        groceryDetail.create(
                            context = context,
                            id = config.itemId,
                            output = ::onDetailOutput,
                        )
                )

            is Configuration.RecipeRoot ->
                RootBloc.Child.RecipeRoot(
                    bloc =
                        recipeRoot.create(
                            context = context,
                            props = config.props,
                            output = ::handleRecipeRootOutput,
                        )
                )

            is Configuration.Authentication ->
                RootBloc.Child.Authentication(
                    bloc =
                        authentication.create(
                            context = context,
                            props = config.props,
                            output = ::handleAuthenticationOutput,
                        )
                )

            is Configuration.Browser ->
                RootBloc.Child.Browser(
                    bloc =
                        browserBlocFactory
                            .create(
                                context = context,
                                output = { navigation.pop() },
                                showControls = false,
                            )
                            .also { bloc ->
                                bloc.onUrlChanged(config.url)
                                bloc.onNavigate()
                            }
                )
        }

    private fun handleBottomNavOutput(output: BottomNavBloc.Output) {
        when (output) {
            BottomNavBloc.Output.AddNewRecipe -> {
                navigation.bringToFront(RecipeRoot(RecipeRootBloc.Props.Create))
            }

            is BottomNavBloc.Output.OpenGrocery -> {
                navigation.bringToFront(GroceryDetail(output.groceryId))
            }

            is BottomNavBloc.Output.OpenRecipe -> {
                navigation.bringToFront(RecipeRoot(Detail(output.recipeId)))
            }

            BottomNavBloc.Output.OpenSignIn -> {
                navigation.bringToFront(
                    Configuration.Authentication(AuthenticationBloc.Props.SignIn)
                )
            }

            BottomNavBloc.Output.OpenSignUp -> {
                navigation.bringToFront(
                    Configuration.Authentication(AuthenticationBloc.Props.SignUp)
                )
            }

            is BottomNavBloc.Output.OpenUrl -> {
                navigation.bringToFront(Configuration.Browser(output.url))
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

    private fun handleAuthenticationOutput(output: AuthenticationBloc.Output) {
        when (output) {
            AuthenticationBloc.Output.Finished -> navigation.pop()
            AuthenticationBloc.Output.AuthenticationSuccess -> navigation.pop()
            is AuthenticationBloc.Output.EmailVerificationRequired -> {
                navigation.pop()
            }
        }
    }

    @Serializable
    private sealed class Configuration {
        @Serializable data object BottomNavigation : Configuration()

        @Serializable data class GroceryDetail(val itemId: Long) : Configuration()

        @Serializable data class RecipeRoot(val props: RecipeRootBloc.Props) : Configuration()

        @Serializable
        data class Authentication(val props: AuthenticationBloc.Props) : Configuration()

        @Serializable data class Browser(val url: String) : Configuration()
    }
}

@ContributesTo(AppScope::class)
interface RootBlocBindingModule {
    @Provides
    fun provideRootBlocFactory(factory: RootBlocImpl.ManagedFactory): RootBloc.Factory =
        RootBloc.Factory { context ->
            factory.create(context)
        }
}
