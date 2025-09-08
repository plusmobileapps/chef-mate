package com.plusmobileapps.chefmate

import com.arkivanov.decompose.ComponentContext
import com.plusmobileapps.chefmate.root.RootBloc
import com.plusmobileapps.chefmate.root.RootBlocFactory

fun buildRoot(
    componentContext: ComponentContext,
    applicationComponent: ApplicationComponent,
): RootBloc = RootBlocFactory.create(
    context = DefaultBlocContext(
        componentContext = componentContext,
    ),
    appComponent = applicationComponent,
)