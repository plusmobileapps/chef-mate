@file:OptIn(ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import chefmate.client.ui.public.generated.resources.Res
import chefmate.client.ui.public.generated.resources.back
import chefmate.client.ui.public.generated.resources.close
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlusHeaderContainer(
    data: PlusHeaderData,
    modifier: Modifier = Modifier,
    scrollEnabled: Boolean = true,
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(ChefMateTheme.colorScheme.background),
    ) {
        if (data !is PlusHeaderData.None) {
            PlusHeader(
                data = data,
            )
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier =
                    if (scrollEnabled) {
                        Modifier
                            .fillMaxHeight()
                            .widthIn(max = 600.dp)
                            .scaffoldContentInsetPadding()
                            .verticalScroll(scrollState)
                    } else {
                        Modifier
                            .fillMaxHeight()
                            .widthIn(max = 600.dp)
                            .scaffoldContentInsetPadding()
                    },
            ) {
                content()
                Spacer(modifier = Modifier.padding(WindowInsets.systemGestures.asPaddingValues()))
            }
            Column {
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier =
                        Modifier.padding(
                            end =
                                with(density) {
                                    WindowInsets.displayCutout
                                        .getRight(
                                            density,
                                            LayoutDirection.Ltr,
                                        ).toDp()
                                },
                        ),
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier.padding(end = ChefMateTheme.dimens.paddingNormal),
                    ) {
                        floatingActionButton()
                    }
                }

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = ChefMateTheme.dimens.paddingNormal),
                    contentAlignment = Alignment.Center,
                ) {
                    snackbarHost()
                }
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
            }
        }
    }
}

@Composable
fun PlusHeader(
    data: PlusHeaderData,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    TopAppBar(
        modifier = modifier,
        windowInsets =
            WindowInsets(
                left = WindowInsets.displayCutout.getLeft(density, LayoutDirection.Ltr),
                right = WindowInsets.displayCutout.getRight(density, LayoutDirection.Ltr),
                top = WindowInsets.statusBars.getTop(density),
                bottom = WindowInsets.statusBars.getBottom(density),
            ),
        title = {
            Text(
                text = data.title.localized(),
                color = ChefMateTheme.colorScheme.onBackground,
            )
        },
        navigationIcon =
            when (data) {
                is PlusHeaderData.Child -> {
                    {
                        IconButton(
                            onClick = data.onBackClick,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back),
                                tint = ChefMateTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }

                is PlusHeaderData.Modal -> {
                    {
                        IconButton(
                            onClick = data.onCloseClick,
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(Res.string.close),
                                tint = ChefMateTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }

                is PlusHeaderData.Parent -> {
                    {  }
                }
                PlusHeaderData.None -> {
                    { }
                }
            },
        actions = {
            when (val trailingAccessory = data.trailingAccessory) {
                is PlusHeaderData.TrailingAccessory.Button -> {
                    PlusButton(
                        text = trailingAccessory.text,
                        onClick = trailingAccessory.onClick,
                    )
                }

                is PlusHeaderData.TrailingAccessory.Custom -> trailingAccessory.content(this)
                null -> Unit
                is PlusHeaderData.TrailingAccessory.Icon -> {
                    if (trailingAccessory.onClick != null) {
                        IconButton(
                            onClick = trailingAccessory.onClick,
                        ) {
                            Icon(
                                trailingAccessory.icon,
                                contentDescription = trailingAccessory.contentDesc.localized(),
                                tint = ChefMateTheme.colorScheme.onBackground,
                            )
                        }
                    } else {
                        Icon(
                            trailingAccessory.icon,
                            contentDescription = trailingAccessory.contentDesc.localized(),
                            tint = ChefMateTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        },
    )
}