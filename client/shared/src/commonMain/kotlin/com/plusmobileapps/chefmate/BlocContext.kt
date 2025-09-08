package com.plusmobileapps.chefmate

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ComponentContextFactory
import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.statekeeper.StateKeeperOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

interface BlocContext : GenericComponentContext<BlocContext> {

    val mainContext: CoroutineContext

    val ioContext: CoroutineContext

    val defaultContext: CoroutineContext

    val unconfinedContext: CoroutineContext

    fun createScope(): CoroutineScope
}

class DefaultBlocContext(
    componentContext: ComponentContext,
    override val mainContext: CoroutineContext = Dispatchers.Main,
    override val ioContext: CoroutineContext = Dispatchers.IO,
    override val defaultContext: CoroutineContext = Dispatchers.Default,
    override val unconfinedContext: CoroutineContext = Dispatchers.Unconfined,
) : BlocContext,
    LifecycleOwner by componentContext,
    StateKeeperOwner by componentContext,
    InstanceKeeperOwner by componentContext,
    BackHandlerOwner by componentContext {

    override val componentContextFactory: ComponentContextFactory<BlocContext> =
        ComponentContextFactory { lifecycle, stateKeeper, instanceKeeper, backHandler ->
            val ctx = componentContext.componentContextFactory(
                lifecycle = lifecycle,
                stateKeeper = stateKeeper,
                instanceKeeper = instanceKeeper,
                backHandler = backHandler
            )
            DefaultBlocContext(
                componentContext = ctx,
                mainContext = mainContext,
                ioContext = ioContext,
                defaultContext = defaultContext,
                unconfinedContext = unconfinedContext
            )
        }

    override fun createScope(): CoroutineScope {
        return coroutineScope(mainContext + SupervisorJob())
    }
}