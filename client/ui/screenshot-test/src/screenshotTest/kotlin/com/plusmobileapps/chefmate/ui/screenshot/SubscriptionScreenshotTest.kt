package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.subscription.ui.previewSubscriptionErrorBloc
import com.plusmobileapps.chefmate.subscription.ui.previewSubscriptionLoadingBloc
import com.plusmobileapps.chefmate.subscription.ui.previewSubscriptionPaywallBloc
import com.plusmobileapps.chefmate.subscription.ui.previewSubscriptionPremiumBloc
import com.plusmobileapps.chefmate.subscription.ui.previewSubscriptionUnavailableBloc
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun SubscriptionPaywallLightScreenshot() {
    ChefMateTheme { previewSubscriptionPaywallBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 700, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SubscriptionPaywallDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewSubscriptionPaywallBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun SubscriptionLoadingScreenshot() {
    ChefMateTheme { previewSubscriptionLoadingBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun SubscriptionPremiumScreenshot() {
    ChefMateTheme { previewSubscriptionPremiumBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun SubscriptionUnavailableScreenshot() {
    ChefMateTheme { previewSubscriptionUnavailableBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun SubscriptionErrorScreenshot() {
    ChefMateTheme { previewSubscriptionErrorBloc.Content() }
}
