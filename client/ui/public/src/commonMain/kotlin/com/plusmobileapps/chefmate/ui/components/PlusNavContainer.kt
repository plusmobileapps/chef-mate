package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A container to embed with a [PlusNavContainer].
 */
@Composable
fun PlusNavContainer(
    data: PlusHeaderData,
    scrollEnabled: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        PlusHeader(
            data = data,
            windowInsets = WindowInsets(),
        )

        val containerModifier =
            if (scrollEnabled) {
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            } else {
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            }

        Column(
            modifier = containerModifier,
            verticalArrangement = verticalArrangement,
        ) {
            content()
        }
    }
}
