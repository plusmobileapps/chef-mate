@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp

/**
 * The app's standard loading spinner: a wavy circular indicator used everywhere in place of the raw
 * Material [androidx.compose.material3.CircularProgressIndicator], so every loading state reads the
 * same across screens, dialogs, buttons, and sync badges.
 *
 * @param color the indicator color. Override it for spinners drawn on a colored surface (e.g. white
 *   on a filled button) so they stay legible.
 * @param strokeWidth the active-indicator stroke width. Leave null for the default; set a smaller
 *   value for compact inline spinners (buttons, chips, sync badges).
 */
@Composable
fun PlusLoadingIndicator(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    color: Color = WavyProgressIndicatorDefaults.indicatorColor,
    strokeWidth: Dp? = null,
) {
    CircularWavyProgressIndicator(
        modifier = modifier.semantics { this.contentDescription = contentDescription.orEmpty() },
        color = color,
        stroke =
            if (strokeWidth == null) {
                WavyProgressIndicatorDefaults.circularIndicatorStroke
            } else {
                Stroke(
                    width = with(LocalDensity.current) { strokeWidth.toPx() },
                    cap = StrokeCap.Round,
                )
            },
    )
}
