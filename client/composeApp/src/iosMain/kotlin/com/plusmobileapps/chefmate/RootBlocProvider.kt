package com.plusmobileapps.chefmate

import com.arkivanov.decompose.ComponentContext
import com.plusmobileapps.chefmate.di.IosApplicationComponent
import com.plusmobileapps.chefmate.di.createIosAppComponent
import com.plusmobileapps.chefmate.root.RootBloc
import platform.UIKit.UIApplication

object RootBlocProvider {
    fun buildRootBloc(
        componentContext: ComponentContext,
        application: UIApplication,
    ): RootBloc {
        val applicationComponent = IosApplicationComponent::class.createIosAppComponent(application)
        return applicationComponent.rootBlocFactory.create(
            DefaultBlocContext(
                componentContext = componentContext,
            ),
        )
    }
}
