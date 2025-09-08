package com.plusmobileapps.chefmate

import com.arkivanov.decompose.ComponentContext
import com.plusmobileapps.chefmate.root.RootBlocFactory

fun buildRootBloc(
    componentContext: ComponentContext,
) = RootBlocFactory.create(
    context = DefaultBlocContext(
        componentContext = componentContext,
    )
)