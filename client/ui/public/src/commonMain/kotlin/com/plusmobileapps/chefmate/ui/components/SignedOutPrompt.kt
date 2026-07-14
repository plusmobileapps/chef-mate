package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import chefmate.client.ui.public.generated.resources.Res
import chefmate.client.ui.public.generated.resources.sign_in
import chefmate.client.ui.public.generated.resources.sign_up
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

/**
 * Prompt shown to a signed-out (or anonymous) user, explaining why an account matters and offering
 * Sign In / Sign Up buttons that open the authentication flow. Shared by the Notifications screen
 * and the grocery-list collaboration section so the affordance stays consistent.
 *
 * The button labels are owned here; [title] and [message] vary per screen. Default layout is
 * start-aligned to sit inside a section — pass [horizontalAlignment] =
 * [Alignment.CenterHorizontally] for a centered empty-state layout. [signInButtonModifier] /
 * [signUpButtonModifier] let callers attach their own test tags.
 */
@Composable
fun SignedOutPrompt(
    title: TextData,
    message: TextData,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    signInButtonModifier: Modifier = Modifier,
    signUpButtonModifier: Modifier = Modifier,
) {
    val textAlign =
        if (horizontalAlignment == Alignment.CenterHorizontally) {
            TextAlign.Center
        } else {
            TextAlign.Start
        }
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        Text(
            text = title.localized(),
            style = ChefMateTheme.typography.titleMedium,
            textAlign = textAlign,
        )
        Text(
            text = message.localized(),
            style = ChefMateTheme.typography.bodyMedium,
            color = ChefMateTheme.colorScheme.onSurfaceVariant,
            textAlign = textAlign,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall)) {
            PlusButton(
                text = Res.string.sign_in.asTextData(),
                onClick = onSignInClick,
                modifier = signInButtonModifier,
            )
            PlusButton(
                text = Res.string.sign_up.asTextData(),
                variant = PlusButtonVariant.SECONDARY,
                onClick = onSignUpClick,
                modifier = signUpButtonModifier,
            )
        }
    }
}
