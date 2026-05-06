@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import chefmate.client.ui.public.generated.resources.Res
import chefmate.client.ui.public.generated.resources.back
import chefmate.client.ui.public.generated.resources.close
import com.plusmobileapps.chefmate.ui.sharedBoundsBy
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlusHeaderContainer(
    data: PlusHeaderData,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    scrollEnabled: Boolean = true,
    maxContentWidth: Dp = 600.dp,
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingToolbar: (@Composable () -> Unit)? = null,
    floatingHeader: Boolean = false,
    headerContainerAlpha: Float = 1f,
    centerAlignTitle: Boolean = false,
    // When true the container adds status-bar + app-bar top padding to the content so it clears
    // the floating header. Pass false when the content manages its own top inset (e.g. Cook Mode).
    floatingHeaderTopReserve: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    if (floatingHeader) {
        val topPadding =
            if (floatingHeaderTopReserve) {
                with(density) { WindowInsets.statusBars.getTop(density).toDp() } + 64.dp
            } else {
                0.dp
            }
        Box(modifier = modifier.fillMaxSize().background(ChefMateTheme.colorScheme.background)) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = ChefMateTheme.colorScheme.background,
                contentColor = ChefMateTheme.colorScheme.onBackground,
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ScrollingContent(
                        scrollEnabled = scrollEnabled,
                        scrollState = scrollState,
                        maxContentWidth = maxContentWidth,
                        topPadding = topPadding,
                        content = content,
                    )
                    BottomBarBox(
                        density = density,
                        floatingActionButton = floatingActionButton,
                        snackbarHost = snackbarHost,
                    )
                    floatingToolbar?.let {
                        Box(modifier = Modifier.align(BottomCenter).floatingToolbarPadding()) {
                            it()
                        }
                    }
                }
            }
            if (data !is PlusHeaderData.None) {
                PlusHeader(
                    data = data,
                    containerAlpha = headerContainerAlpha,
                    centerAlign = centerAlignTitle,
                    windowInsets =
                        WindowInsets.systemBars
                            .union(WindowInsets.displayCutout)
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize().background(ChefMateTheme.colorScheme.background),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
        ) {
            if (data !is PlusHeaderData.None) {
                PlusHeader(data = data)
            }

            Surface(
                modifier = Modifier.weight(1f),
                color = ChefMateTheme.colorScheme.background,
                contentColor = ChefMateTheme.colorScheme.onBackground,
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ScrollingContent(
                        scrollEnabled = scrollEnabled,
                        scrollState = scrollState,
                        maxContentWidth = maxContentWidth,
                        content = content,
                    )
                    BottomBarBox(
                        density = density,
                        floatingActionButton = floatingActionButton,
                        snackbarHost = snackbarHost,
                    )

                    floatingToolbar?.let {
                        Box(modifier = Modifier.align(BottomCenter).floatingToolbarPadding()) {
                            it()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBarBox(
    density: Density,
    floatingActionButton: @Composable (() -> Unit),
    snackbarHost: @Composable (() -> Unit),
) {
    Column {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier =
                Modifier.padding(
                    end =
                        with(density) {
                            WindowInsets.displayCutout.getRight(density, LayoutDirection.Ltr).toDp()
                        }
                )
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.padding(end = ChefMateTheme.dimens.paddingNormal)) {
                floatingActionButton()
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(top = ChefMateTheme.dimens.paddingNormal),
            contentAlignment = Alignment.Center,
        ) {
            snackbarHost()
        }
        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
    }
}

@Composable
private fun ScrollingContent(
    modifier: Modifier = Modifier,
    scrollEnabled: Boolean,
    scrollState: ScrollState,
    maxContentWidth: Dp,
    topPadding: Dp = 0.dp,
    content: @Composable (ColumnScope.() -> Unit),
) {
    Column(
        modifier =
            if (scrollEnabled) {
                modifier
                    .fillMaxHeight()
                    .widthIn(max = maxContentWidth)
                    .padding(top = topPadding)
                    .scaffoldContentInsetPadding()
                    .verticalScroll(scrollState)
            } else {
                modifier
                    .fillMaxHeight()
                    .widthIn(max = maxContentWidth)
                    .padding(top = topPadding)
                    .scaffoldContentInsetPadding()
            }
    ) {
        content()
        if (scrollEnabled) {
            Spacer(modifier = Modifier.padding(WindowInsets.systemGestures.asPaddingValues()))
        }
    }
}

@Composable
fun PlusHeader(
    data: PlusHeaderData,
    windowInsets: WindowInsets? = null,
    containerAlpha: Float = 1f,
    centerAlign: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val resolvedInsets =
        windowInsets
            ?: WindowInsets(
                left = WindowInsets.displayCutout.getLeft(density, LayoutDirection.Ltr),
                right = WindowInsets.displayCutout.getRight(density, LayoutDirection.Ltr),
                top = WindowInsets.statusBars.getTop(density),
                bottom = WindowInsets.statusBars.getBottom(density),
            )
    val colors =
        if (containerAlpha < 1f) {
            TopAppBarDefaults.topAppBarColors(
                containerColor = ChefMateTheme.colorScheme.background.copy(alpha = containerAlpha)
            )
        } else {
            TopAppBarDefaults.topAppBarColors()
        }
    val title: @Composable () -> Unit = {
        val titleKey = (data as? PlusHeaderData.Child)?.titleSharedElementKey
        Text(
            text = data.title.localized(),
            color = ChefMateTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.sharedBoundsBy(titleKey),
        )
    }
    val navigationIcon: @Composable () -> Unit =
        when (data) {
            is PlusHeaderData.Child -> {
                {
                    PlusIconButton(
                        modifier = Modifier.padding(end = ChefMateTheme.dimens.paddingSmall),
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.back),
                        onClick = data.onBackClick,
                    )
                }
            }
            is PlusHeaderData.Modal -> {
                {
                    PlusIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.close),
                        onClick = data.onCloseClick,
                    )
                }
            }
            is PlusHeaderData.Parent,
            PlusHeaderData.None -> {
                {}
            }
        }
    val actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
        when (val trailingAccessory = data.trailingAccessory) {
            is PlusHeaderData.TrailingAccessory.Button -> {
                PlusButton(text = trailingAccessory.text, onClick = trailingAccessory.onClick)
            }
            is PlusHeaderData.TrailingAccessory.Custom -> trailingAccessory.content(this)
            null -> Unit
            is PlusHeaderData.TrailingAccessory.Icon -> {
                if (trailingAccessory.onClick != null) {
                    IconButton(onClick = trailingAccessory.onClick) {
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
    }
    if (centerAlign) {
        CenterAlignedTopAppBar(
            modifier = modifier,
            windowInsets = resolvedInsets,
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
        )
    } else {
        TopAppBar(
            modifier = modifier,
            windowInsets = resolvedInsets,
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
        )
    }
}
