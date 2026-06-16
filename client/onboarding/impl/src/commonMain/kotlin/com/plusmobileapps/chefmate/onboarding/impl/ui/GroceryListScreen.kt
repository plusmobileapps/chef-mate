package com.plusmobileapps.chefmate.onboarding.impl.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_grocery_list_message
import chefmate.client.onboarding.public.generated.resources.onboarding_grocery_list_title
import chefmate.client.onboarding.public.generated.resources.onboarding_next
import com.plusmobileapps.chefmate.onboarding.GroceryListBloc
import com.plusmobileapps.chefmate.onboarding.OnboardingTestTags

@Composable
fun GroceryListScreen(bloc: GroceryListBloc, modifier: Modifier = Modifier) {
    OnboardingInfoLayout(
        icon = Icons.Default.AddShoppingCart,
        title = Res.string.onboarding_grocery_list_title,
        message = Res.string.onboarding_grocery_list_message,
        buttonText = Res.string.onboarding_next,
        onButtonClick = bloc::onNextClicked,
        screenTestTag = OnboardingTestTags.GROCERY_LIST_SCREEN,
        buttonTestTag = OnboardingTestTags.GROCERY_LIST_NEXT_BUTTON,
        modifier = modifier,
    )
}
