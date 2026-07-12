package com.plusmobileapps.chefmate.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusButton
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Shared layout for the icon-driven onboarding steps: a large themed [icon] standing in for the
 * feature (used where there is no real screen capture to frame), a title, and a body, with a single
 * primary "Next"-style button pinned to the bottom. This is the icon counterpart to
 * [OnboardingInfoLayout], which frames an actual screenshot instead.
 */
@Composable
internal fun OnboardingIconLayout(
    icon: ImageVector,
    title: StringResource,
    message: StringResource,
    buttonText: StringResource,
    onButtonClick: () -> Unit,
    screenTestTag: String,
    buttonTestTag: String,
    modifier: Modifier = Modifier,
) {
    OnboardingStepScaffold(
        screenTestTag = screenTestTag,
        modifier = modifier,
        footer = {
            PlusButton(
                text = buttonText.asTextData(),
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth().testTag(buttonTestTag),
            )
        },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // decorative; the title and message describe the feature
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
    }
}
