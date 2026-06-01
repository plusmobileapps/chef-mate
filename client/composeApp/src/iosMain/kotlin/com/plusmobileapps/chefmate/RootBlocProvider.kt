package com.plusmobileapps.chefmate

import com.arkivanov.decompose.ComponentContext
import com.plusmobileapps.chefmate.di.IosApplicationComponent
import com.plusmobileapps.chefmate.root.DeepLink
import com.plusmobileapps.chefmate.root.RootBloc
import dev.zacsweers.metro.createGraphFactory
import platform.UIKit.UIApplication

object RootBlocProvider {
    fun buildRootBloc(
        componentContext: ComponentContext,
        application: UIApplication,
        deepLinkUrl: String? = null,
    ): RootBloc {
        val applicationComponent =
            createGraphFactory<IosApplicationComponent.Factory>().create(application)
        return applicationComponent.rootBlocFactory.create(
            context = DefaultBlocContext(componentContext = componentContext),
            deepLink = DeepLink.parse(deepLinkUrl),
        )
    }
}
