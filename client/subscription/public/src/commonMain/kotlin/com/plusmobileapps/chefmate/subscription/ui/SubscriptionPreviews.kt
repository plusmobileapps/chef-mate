package com.plusmobileapps.chefmate.subscription.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.subscription.SubscriptionBloc
import com.plusmobileapps.chefmate.subscription.data.SubscriptionPackage
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.MutableStateFlow

private fun subscriptionBloc(model: SubscriptionBloc.Model): SubscriptionBloc =
    object : SubscriptionBloc {
        override val state = MutableStateFlow(model)

        override fun onCloseClicked() = Unit

        override fun onPackageSelected(packageId: String) = Unit

        override fun onPurchaseClicked() = Unit

        override fun onRestoreClicked() = Unit

        override fun onErrorDismissed() = Unit

        @Composable
        override fun Content(modifier: Modifier) =
            SubscriptionScreen(bloc = this, modifier = modifier)
    }

private val samplePackages =
    listOf(
        SubscriptionPackage(
            id = "monthly",
            title = "Monthly",
            description = "Billed monthly, cancel anytime",
            priceFormatted = "$4.99",
        ),
        SubscriptionPackage(
            id = "yearly",
            title = "Yearly",
            description = "Billed annually — best value",
            priceFormatted = "$39.99",
        ),
    )

val previewSubscriptionPaywallBloc: SubscriptionBloc =
    subscriptionBloc(
        SubscriptionBloc.Model(
            isLoading = false,
            packages = samplePackages,
            selectedPackageId = "yearly",
        )
    )

val previewSubscriptionLoadingBloc: SubscriptionBloc =
    subscriptionBloc(SubscriptionBloc.Model(isLoading = true))

val previewSubscriptionPremiumBloc: SubscriptionBloc =
    subscriptionBloc(SubscriptionBloc.Model(isLoading = false, isPremium = true))

val previewSubscriptionUnavailableBloc: SubscriptionBloc =
    subscriptionBloc(SubscriptionBloc.Model(isLoading = false, packages = emptyList()))

val previewSubscriptionErrorBloc: SubscriptionBloc =
    subscriptionBloc(
        SubscriptionBloc.Model(
            isLoading = false,
            packages = samplePackages,
            selectedPackageId = "monthly",
            error = FixedString("We couldn’t complete that. Please try again."),
        )
    )

@Preview(showBackground = true, heightDp = 700)
@Composable
internal fun SubscriptionPaywallPreview() {
    ChefMateTheme { previewSubscriptionPaywallBloc.Content() }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
internal fun SubscriptionLoadingPreview() {
    ChefMateTheme { previewSubscriptionLoadingBloc.Content() }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
internal fun SubscriptionPremiumPreview() {
    ChefMateTheme { previewSubscriptionPremiumBloc.Content() }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
internal fun SubscriptionPaywallDarkPreview() {
    ChefMateTheme(darkTheme = true) { previewSubscriptionPaywallBloc.Content() }
}
