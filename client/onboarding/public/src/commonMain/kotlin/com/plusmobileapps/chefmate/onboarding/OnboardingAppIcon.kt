package com.plusmobileapps.chefmate.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_app_icon
import org.jetbrains.compose.resources.painterResource

@Composable
fun OnboardingAppIcon(modifier: Modifier = Modifier, size: Dp = 200.dp) {
    Image(
        painter = painterResource(Res.drawable.onboarding_app_icon),
        contentDescription = null, // decorative; the title and message describe the app
        modifier = modifier.size(size).clip(RoundedCornerShape(20.dp)),
    )
}
