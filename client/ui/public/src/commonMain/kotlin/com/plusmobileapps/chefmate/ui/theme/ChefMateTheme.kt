@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.plusmobileapps.chefmate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorPalette =
    darkColorScheme(
        primary = purple200,
//    secondary = purple700,
        secondary = teal200,
    )

private val LightColorPalette =
    lightColorScheme(
        primary = purple500,
//    secondary = purple700,
        secondary = teal200,
    )

@Composable
fun ChefMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val dimensions = AppDimensions()
    val colors =
        if (darkTheme) {
            DarkColorPalette
        } else {
            LightColorPalette
        }

    CompositionLocalProvider(
        LocalDimensions provides dimensions,
    ) {
        MaterialExpressiveTheme(
            colorScheme = colors,
            typography = typography,
            shapes = shapes,
            content = content,
        )
    }
}

object ChefMateTheme {
    val colorScheme: ColorScheme
        @Composable
        get() = MaterialTheme.colorScheme

    val dimens: AppDimensions
        @Composable
        get() = LocalDimensions.current

    val shapes: Shapes
        @Composable
        get() = MaterialTheme.shapes

    val typography: Typography
        @Composable
        get() = MaterialTheme.typography
}