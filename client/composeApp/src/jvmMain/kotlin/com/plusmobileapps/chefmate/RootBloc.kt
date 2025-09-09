package com.plusmobileapps.chefmate

import com.arkivanov.decompose.ComponentContext
import com.plusmobileapps.chefmate.root.RootBloc

fun buildRoot(
    componentContext: ComponentContext,
    applicationComponent: ApplicationComponent,
): RootBloc = applicationComponent.rootBlocFactory.create(
    DefaultBlocContext(
        componentContext = componentContext,
    )
)