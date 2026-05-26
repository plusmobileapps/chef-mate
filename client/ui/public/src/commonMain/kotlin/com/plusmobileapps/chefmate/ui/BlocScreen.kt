package com.plusmobileapps.chefmate.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface BlocScreen {
    @Composable fun Content(modifier: Modifier = Modifier)
}
