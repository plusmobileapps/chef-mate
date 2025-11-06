@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
fun PlusLoadingIndicator(
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
) {
    CircularWavyProgressIndicator(
        modifier = modifier.semantics {
            this.contentDescription = contentDescription.orEmpty()
        }
    )
}