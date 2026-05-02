@file:OptIn(DelicateDecomposeApi::class)

package com.plusmobileapps.chefmate.browser.impl

import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.browser.BrowserBloc
import com.plusmobileapps.chefmate.browser.BrowserEditQueryBloc
import com.plusmobileapps.chefmate.browser.BrowserLandingBloc
import com.plusmobileapps.chefmate.browser.BrowserRootBloc
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@AssistedInject
class BrowserRootBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<BrowserRootBloc.Output>,
    @Assisted private val initialUrl: String?,
    @Assisted private val showControls: Boolean,
    private val browserBlocFactory: BrowserBloc.Factory,
    private val landingBlocFactory: BrowserLandingBloc.Factory,
    private val editQueryBlocFactory: BrowserEditQueryBloc.Factory,
) : BrowserRootBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(
            context: BlocContext,
            output: Consumer<BrowserRootBloc.Output>,
            initialUrl: String?,
            showControls: Boolean,
        ): BrowserRootBlocImpl
    }

    private val navigation = StackNavigation<Configuration>()
    private val scope = createScope()

    private val webViewBackCallback =
        BackCallback(isEnabled = false) {
            val activeChild = stack.value.active.instance
            if (activeChild is BrowserRootBloc.Child.Browser) {
                activeChild.bloc.onGoBack()
            }
        }

    private val stack =
        childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialStack = {
                if (initialUrl != null) listOf(Configuration.Browser(initialUrl))
                else listOf(Configuration.Landing())
            },
            handleBackButton = true,
            key = "BrowserRootRouter",
            childFactory = ::createChild,
        )

    init {
        backHandler.register(webViewBackCallback)
        observeCanGoBack()
    }

    override val routerState: Value<ChildStack<*, BrowserRootBloc.Child>> = stack

    override fun navigateToUrl(url: String) {
        navigation.replaceAll(Configuration.Browser(url))
    }

    private fun observeCanGoBack() {
        var stackCancellation: Cancellation? = null
        var canGoBackJob: Job? = null

        lifecycle.doOnResume {
            stackCancellation = stack.subscribe { childStack ->
                canGoBackJob?.cancel()
                val activeChild = childStack.active.instance
                if (activeChild is BrowserRootBloc.Child.Browser) {
                    canGoBackJob = scope.launch {
                        activeChild.bloc.state.collect { state ->
                            webViewBackCallback.isEnabled = state.canGoBack
                        }
                    }
                } else {
                    webViewBackCallback.isEnabled = false
                }
            }
        }

        lifecycle.doOnPause {
            canGoBackJob?.cancel()
            canGoBackJob = null
            stackCancellation?.cancel()
            stackCancellation = null
            webViewBackCallback.isEnabled = false
        }
    }

    private fun createChild(config: Configuration, context: BlocContext): BrowserRootBloc.Child =
        when (config) {
            is Configuration.Landing ->
                BrowserRootBloc.Child.Landing(
                    landingBlocFactory
                        .create(context) { landingOutput ->
                            when (landingOutput) {
                                is BrowserLandingBloc.Output.Navigate ->
                                    navigation.replaceAll(Configuration.Browser(landingOutput.url))
                                is BrowserLandingBloc.Output.OpenEditQuery ->
                                    navigation.push(
                                        Configuration.EditQuery(landingOutput.initialText)
                                    )
                            }
                        }
                        .also { bloc ->
                            if (config.initialText.isNotEmpty()) {
                                bloc.onSearchTextChanged(config.initialText)
                            }
                        }
                )
            is Configuration.EditQuery ->
                BrowserRootBloc.Child.EditQuery(
                    editQueryBlocFactory.create(
                        context = context,
                        output = { editOutput ->
                            when (editOutput) {
                                is BrowserEditQueryBloc.Output.Navigate ->
                                    navigation.replaceAll(Configuration.Browser(editOutput.url))
                                BrowserEditQueryBloc.Output.Cancel -> navigation.pop()
                            }
                        },
                        initialText = config.initialText,
                    )
                )
            is Configuration.Browser ->
                BrowserRootBloc.Child.Browser(
                    browserBlocFactory
                        .create(
                            context = context,
                            output = { blocOutput ->
                                when (blocOutput) {
                                    is BrowserBloc.Output.RecipeExtracted ->
                                        output.onNext(
                                            BrowserRootBloc.Output.RecipeExtracted(
                                                blocOutput.recipeId
                                            )
                                        )
                                    is BrowserBloc.Output.NavigateToLanding ->
                                        navigation.push(
                                            Configuration.EditQuery(blocOutput.initialText)
                                        )
                                }
                            },
                            showControls = showControls,
                        )
                        .also { bloc ->
                            bloc.onUrlChanged(config.url)
                            bloc.onNavigate()
                        }
                )
        }

    @Serializable
    private sealed class Configuration {
        @Serializable data class Landing(val initialText: String = "") : Configuration()

        @Serializable data class EditQuery(val initialText: String) : Configuration()

        @Serializable data class Browser(val url: String) : Configuration()
    }
}

@ContributesTo(AppScope::class)
interface BrowserRootBlocBindingModule {
    @Provides
    fun provideBrowserRootBlocFactory(
        factory: BrowserRootBlocImpl.ManagedFactory
    ): BrowserRootBloc.Factory =
        object : BrowserRootBloc.Factory {
            override fun create(
                context: BlocContext,
                output: Consumer<BrowserRootBloc.Output>,
                initialUrl: String?,
                showControls: Boolean,
            ): BrowserRootBloc = factory.create(context, output, initialUrl, showControls)
        }
}
