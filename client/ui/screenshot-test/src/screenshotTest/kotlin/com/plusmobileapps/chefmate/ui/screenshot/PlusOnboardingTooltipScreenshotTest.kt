package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusTooltipBubble
import com.plusmobileapps.chefmate.ui.components.PlusTooltipPlacement
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
private fun TooltipBubblePreview(placement: PlusTooltipPlacement, darkTheme: Boolean = false) {
    ChefMateTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                PlusTooltipBubble(
                    text = "Tap here to cook hands-free in Cook Mode".asTextData(),
                    placement = placement,
                )
            }
        }
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360)
@Composable
fun PlusTooltipBubbleAboveScreenshot() {
    TooltipBubblePreview(placement = PlusTooltipPlacement.ABOVE)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360)
@Composable
fun PlusTooltipBubbleBelowScreenshot() {
    TooltipBubblePreview(placement = PlusTooltipPlacement.BELOW)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PlusTooltipBubbleAboveDarkScreenshot() {
    TooltipBubblePreview(placement = PlusTooltipPlacement.ABOVE, darkTheme = true)
}
