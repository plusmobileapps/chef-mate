package com.plusmobileapps.chefmate.subscription

import androidx.compose.runtime.Composable
import chefmate.client.subscription.public.generated.resources.Res
import chefmate.client.subscription.public.generated.resources.subscription_premium_required_confirm
import chefmate.client.subscription.public.generated.resources.subscription_premium_required_dismiss
import chefmate.client.subscription.public.generated.resources.subscription_premium_required_message
import chefmate.client.subscription.public.generated.resources.subscription_premium_required_title
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusDialog

/**
 * Warning shown at the root when a non-subscriber taps a premium feature (currently AI chat).
 * Confirming opens the paywall; dismissing returns to where they were.
 */
@Composable
fun PremiumRequiredDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    PlusDialog(
        title = Res.string.subscription_premium_required_title.asTextData(),
        message = Res.string.subscription_premium_required_message.asTextData(),
        confirmButtonText = Res.string.subscription_premium_required_confirm.asTextData(),
        dismissButtonText = Res.string.subscription_premium_required_dismiss.asTextData(),
        onConfirmClick = onConfirm,
        onDismissRequest = onDismiss,
    )
}
