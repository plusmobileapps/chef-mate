package com.plusmobileapps.chefmate.subscription.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import chefmate.client.subscription.public.generated.resources.Res
import chefmate.client.subscription.public.generated.resources.subscription_active_message
import chefmate.client.subscription.public.generated.resources.subscription_active_title
import chefmate.client.subscription.public.generated.resources.subscription_error_title
import chefmate.client.subscription.public.generated.resources.subscription_restore
import chefmate.client.subscription.public.generated.resources.subscription_subscribe
import chefmate.client.subscription.public.generated.resources.subscription_subtitle
import chefmate.client.subscription.public.generated.resources.subscription_title
import chefmate.client.subscription.public.generated.resources.subscription_unavailable
import com.plusmobileapps.chefmate.subscription.SubscriptionBloc
import com.plusmobileapps.chefmate.subscription.SubscriptionTestTags
import com.plusmobileapps.chefmate.subscription.data.SubscriptionPackage
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusButtonVariant
import com.plusmobileapps.chefmate.ui.components.PlusDialog
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun SubscriptionScreen(bloc: SubscriptionBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    state.error?.let { error ->
        PlusDialog(
            title = Res.string.subscription_error_title.asTextData(),
            message = error,
            onConfirmClick = bloc::onErrorDismissed,
            onDismissRequest = bloc::onErrorDismissed,
        )
    }

    PlusNavContainer(
        modifier = modifier.testTag(SubscriptionTestTags.SCREEN),
        data =
            PlusHeaderData.Modal(
                title = Res.string.subscription_title.asTextData(),
                onCloseClick = bloc::onCloseClicked,
            ),
        content = {
            when {
                state.isLoading -> LoadingState()
                state.isPremium -> PremiumState()
                state.packages.isEmpty() -> UnavailableState()
                else -> PaywallContent(state = state, bloc = bloc)
            }
        },
    )
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PlusLoadingIndicator()
    }
}

@Composable
private fun PremiumState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal),
    ) {
        Text(
            Res.string.subscription_active_title.asTextData().localized(),
            style = ChefMateTheme.typography.headlineSmall,
        )
        Text(
            Res.string.subscription_active_message.asTextData().localized(),
            style = ChefMateTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun UnavailableState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            Res.string.subscription_unavailable.asTextData().localized(),
            style = ChefMateTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PaywallContent(state: SubscriptionBloc.Model, bloc: SubscriptionBloc) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal),
    ) {
        Text(
            Res.string.subscription_subtitle.asTextData().localized(),
            style = ChefMateTheme.typography.bodyLarge,
        )
        state.packages.forEach { subscriptionPackage ->
            PackageRow(
                subscriptionPackage = subscriptionPackage,
                selected = subscriptionPackage.id == state.selectedPackageId,
                onClick = { bloc.onPackageSelected(subscriptionPackage.id) },
            )
        }
        PlusButton(
            text = Res.string.subscription_subscribe.asTextData(),
            isLoading = state.isProcessing,
            enabled = state.selectedPackageId != null,
            modifier = Modifier.fillMaxWidth().testTag(SubscriptionTestTags.SUBSCRIBE_BUTTON),
            onClick = bloc::onPurchaseClicked,
        )
        PlusButton(
            text = Res.string.subscription_restore.asTextData(),
            variant = PlusButtonVariant.SECONDARY,
            enabled = !state.isProcessing,
            modifier = Modifier.fillMaxWidth().testTag(SubscriptionTestTags.RESTORE_BUTTON),
            onClick = bloc::onRestoreClicked,
        )
    }
}

@Composable
private fun PackageRow(
    subscriptionPackage: SubscriptionPackage,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .testTag(SubscriptionTestTags.PACKAGE_ROW_PREFIX + subscriptionPackage.id)
                .selectable(selected = selected, onClick = onClick)
                .padding(vertical = ChefMateTheme.dimens.paddingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(subscriptionPackage.title, style = ChefMateTheme.typography.titleMedium)
            if (subscriptionPackage.description.isNotBlank()) {
                Text(
                    subscriptionPackage.description,
                    style = ChefMateTheme.typography.bodySmall,
                    color = ChefMateTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(subscriptionPackage.priceFormatted, style = ChefMateTheme.typography.titleMedium)
    }
}
