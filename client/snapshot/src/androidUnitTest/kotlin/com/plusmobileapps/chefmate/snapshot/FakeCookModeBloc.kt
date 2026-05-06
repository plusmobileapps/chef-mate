package com.plusmobileapps.chefmate.snapshot

import com.plusmobileapps.chefmate.cook.CookModeBloc
import com.plusmobileapps.chefmate.cook.WhatsCookingBloc
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCookModeBloc(
    model: CookModeBloc.Model = CookModeBloc.Model(),
    whatsCooking: WhatsCookingBloc.Model = WhatsCookingBloc.Model(),
) : CookModeBloc {
    override val state = MutableStateFlow(model)
    override val whatsCookingBloc: WhatsCookingBloc = FakeWhatsCookingBloc(whatsCooking)

    override fun onCloseClicked() {}

    override fun onRecipeChipClicked(recipeId: Long) {}

    override fun onLayoutToggled() {}

    override fun onBackClicked() {}
}
