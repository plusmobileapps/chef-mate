package com.plusmobileapps.chefmate

import com.arkivanov.decompose.ComponentContext
import com.plusmobileapps.chefmate.root.DeepLink
import com.plusmobileapps.chefmate.root.RootBloc

fun buildRootBloc(
    componentContext: ComponentContext,
    applicationComponent: ApplicationComponent,
    deepLink: DeepLink = DeepLink.None,
): RootBloc =
    applicationComponent.rootBlocFactory.create(
        context = DefaultBlocContext(componentContext = componentContext),
        deepLink = deepLink,
    )
