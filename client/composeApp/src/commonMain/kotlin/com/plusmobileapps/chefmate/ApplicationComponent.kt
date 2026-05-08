package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.root.RootBloc

interface ApplicationComponent {
    val rootBlocFactory: RootBloc.Factory
    val authenticationRepository: AuthenticationRepository
}
