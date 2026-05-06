package com.plusmobileapps.chefmate.snapshot

import com.plusmobileapps.chefmate.cook.WhatsCookingBloc
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWhatsCookingBloc(model: WhatsCookingBloc.Model = WhatsCookingBloc.Model()) :
    WhatsCookingBloc {
    override val state = MutableStateFlow(model)

    override fun onRecipeClicked(recipeId: Long) {}

    override fun onSelectModeToggled() {}

    override fun onSelectionToggled(recipeId: Long) {}

    override fun onDeleteSelectedClicked() {}

    override fun onCloseClicked() {}
}
