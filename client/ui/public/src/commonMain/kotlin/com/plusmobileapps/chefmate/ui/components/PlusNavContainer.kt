@file:OptIn(ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** A container to embed with a [PlusNavContainer]. */
@Composable
fun PlusNavContainer(
    data: PlusHeaderData,
    scrollEnabled: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (data !is PlusHeaderData.None) {
            PlusHeader(data = data, windowInsets = WindowInsets())
        }

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
        }
    }
}
