package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** A container to embed with a [PlusNavContainer]. */
@Composable
fun PlusNavContainer(
    data: PlusHeaderData,
    scrollEnabled: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        PlusHeader(data = data, windowInsets = WindowInsets())

        val contentModifier =
            if (scrollEnabled) {
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            } else {
                Modifier.fillMaxSize()
            }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(modifier = contentModifier, verticalArrangement = verticalArrangement) {
                content()
            }
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)) {
                snackbarHost()
            }
        }
    }
}
