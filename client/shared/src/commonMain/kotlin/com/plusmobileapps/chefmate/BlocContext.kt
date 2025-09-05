package com.plusmobileapps.chefmate

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ComponentContextFactory
import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.statekeeper.StateKeeperOwner
import com.plusmobileapps.chefmate.database.Database
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.IO

interface BlocContext : GenericComponentContext<BlocContext> {

    val mainContext: CoroutineContext

    val ioContext: CoroutineContext

    val defaultContext: CoroutineContext

    val unconfinedContext: CoroutineContext
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
}