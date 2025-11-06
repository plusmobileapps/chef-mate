package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

data class NavRailItem(
    val label: TextData,
    val selected: Boolean,
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit,
)

@Composable
fun PlusNavRailHeaderContainer(
    modifier: Modifier = Modifier,
    navRail: List<NavRailItem>,
    content: @Composable (PaddingValues) -> Unit,
) {
    val density = LocalDensity.current

    PlusResponsiveContainer(modifier = modifier.fillMaxSize()) { windowSize ->
        Row(
            modifier = modifier.fillMaxSize(),
        ) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                windowInsets =
                    WindowInsets(
                        left = WindowInsets.displayCutout.getLeft(density, LayoutDirection.Ltr),
                        right = 0,
                        top = WindowInsets.statusBars.getTop(density),
                        bottom = WindowInsets.systemGestures.getBottom(density),
                    ),
                content = {
                    when (windowSize) {
                        WindowSizeClass.COMPACT,
                        WindowSizeClass.MEDIUM,
                            -> {
                            navRail.forEach { item ->
                                NavigationRailItem(
                                    selected = item.selected,
                                    onClick = item.onClick,
                                    icon = { item.icon() },
                                    label = { item.label.localized() },
                                )
                            }
                        }

                        WindowSizeClass.EXPANDED -> {
                            navRail.forEach { item ->
                                PlusExpandedNavRailItem(
                                    selected = item.selected,
                                    onClick = item.onClick,
                                    icon = { item.icon() },
                                    label = { Text(item.label.localized()) },
                                )
                            }
                        }
                    }
                },
                containerColor = ChefMateTheme.colorScheme.surfaceContainer,
                contentColor = ChefMateTheme.colorScheme.onSurface,
            )

            Box(
                modifier = Modifier.weight(1f),
            ) {
                val paddingValues: PaddingValues =
                    with(density) {
                        PaddingValues.Absolute(
                            right =
                                WindowInsets.displayCutout
                                    .getRight(
                                        density,
                                        LayoutDirection.Ltr,
                                    ).toDp(),
                            top = WindowInsets.statusBars.getTop(density).toDp(),
                            bottom = WindowInsets.statusBars.getBottom(density).toDp(),
                        )
                    }
                content(paddingValues)
            }
        }
    }
}

@Composable
fun PlusExpandedNavRailItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
) {
    val backgroundColor =
        if (selected) {
            ChefMateTheme.colorScheme.secondaryContainer
        } else {
            ChefMateTheme.colorScheme.surfaceContainer
        }

    val contentColor =
        if (selected) {
            ChefMateTheme.colorScheme.onSecondaryContainer
        } else {
            ChefMateTheme.colorScheme.onSurface
        }

    Row(
        modifier =
            modifier
                .width(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            icon()
            label()
        }
    }
}