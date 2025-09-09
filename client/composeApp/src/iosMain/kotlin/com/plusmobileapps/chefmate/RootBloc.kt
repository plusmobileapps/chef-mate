package com.plusmobileapps.chefmate

import com.arkivanov.decompose.ComponentContext
import com.plusmobileapps.chefmate.di.IosApplicationComponent
import com.plusmobileapps.chefmate.di.createIosAppComponent
import com.plusmobileapps.chefmate.root.RootBloc
import com.plusmobileapps.chefmate.root.RootBlocFactory
import platform.UIKit.UIApplication

fun buildRootBloc(
    componentContext: ComponentContext,
    application: UIApplication,
): RootBloc {
    val applicationComponent = IosApplicationComponent::class.createIosAppComponent(application)
    return RootBlocFactory.create(
        context = DefaultBlocContext(
            componentContext = componentContext,
        ),
        appComponent = applicationComponent,
    )
}