package com.plusmobileapps.chefmate

import com.arkivanov.decompose.ComponentContext
import com.plusmobileapps.chefmate.root.RootBloc
import com.plusmobileapps.chefmate.root.RootBlocFactory

fun buildRootBloc(
    componentContext: ComponentContext,
): RootBloc {
    val applicationComponent = IosApplicationComponent()
    return RootBlocFactory.create(
        context = DefaultBlocContext(
            componentContext = componentContext,
        ),
        appComponent = applicationComponent,
    )
}