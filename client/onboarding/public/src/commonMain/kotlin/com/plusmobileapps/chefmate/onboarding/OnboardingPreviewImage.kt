package com.plusmobileapps.chefmate.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * A framed, phone-shaped preview of a real feature screen shown at the top of an onboarding step so
 * the user can see what the feature looks like. [light]/[dark] are the same captures used by the
 * feature snapshot tests; the variant is picked from the active theme (not the system setting) so
 * it stays correct even when the theme is forced. The image is cropped to its top so the meaningful
 * chrome (title bar, first rows) is what shows.
 */
@Composable
internal fun OnboardingPreviewImage(
    light: DrawableResource,
    dark: DrawableResource,
    modifier: Modifier = Modifier,
) {
    val isDark = ChefMateTheme.colorScheme.surface.luminance() < 0.5f
    val shape = RoundedCornerShape(20.dp)

    Image(
        painter = painterResource(if (isDark) dark else light),
        contentDescription = null, // decorative; the step title and body describe the feature
        contentScale = ContentScale.Crop,
        alignment = Alignment.TopCenter,
        modifier =
            modifier
                .fillMaxWidth()
                .widthIn(max = 240.dp)
                .aspectRatio(3f / 4f)
                .shadow(elevation = 8.dp, shape = shape)
                .clip(shape)
                .border(1.dp, ChefMateTheme.colorScheme.outlineVariant, shape),
    )
}
