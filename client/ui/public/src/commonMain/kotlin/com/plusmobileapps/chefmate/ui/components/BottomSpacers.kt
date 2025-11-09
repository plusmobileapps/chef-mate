package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LastItemWindowInsetSpacer(modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
}

@Composable
fun FloatingActionButtonBottomSpacer(modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.height(100.dp))
}

fun LazyListScope.lastItemWindowInsetSpacer(modifier: Modifier = Modifier) {
    item("lastItemWindowInsetSpacer") {
        LastItemWindowInsetSpacer(modifier = modifier)
    }
}

fun LazyListScope.lastItemFloatingActionButtonSpacer(modifier: Modifier = Modifier) {
    item("lastItemFloatingActionButtonSpacer") {
        Spacer(modifier = modifier.height(100.dp))
    }
}

fun LazyGridScope.lastItemWindowInsetSpacer(modifier: Modifier = Modifier) {
    item("lastItemWindowInsetSpacer") {
        LastItemWindowInsetSpacer(modifier = modifier)
    }
}

fun LazyGridScope.lastItemFloatingActionButtonSpacer(modifier: Modifier = Modifier) {
    item("lastItemFloatingActionButtonSpacer") {
        Spacer(modifier = modifier.height(100.dp))
    }
}
