package com.plusmobileapps.chefmate.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Shared layout for the informational onboarding steps: a centered icon, title, body, and a single
 * primary "Next"-style button. Each step screen supplies its own content, icon, and test tags.
 */
@Composable
internal fun OnboardingInfoLayout(
    icon: ImageVector,
    title: StringResource,
    message: StringResource,
    buttonText: StringResource,
    onButtonClick: () -> Unit,
    screenTestTag: String,
    buttonTestTag: String,
    modifier: Modifier = Modifier,
) {
    val dimens = ChefMateTheme.dimens

    PlusHeaderContainer(
        modifier = modifier.testTag(screenTestTag).fillMaxWidth(),
        data = PlusHeaderData.None,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimens.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(dimens.paddingNormal),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp),
            )
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            PlusButton(
                text = buttonText.asTextData(),
                onClick = onButtonClick,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(top = dimens.paddingNormal)
                        .testTag(buttonTestTag),
            )
        }
    }
}
