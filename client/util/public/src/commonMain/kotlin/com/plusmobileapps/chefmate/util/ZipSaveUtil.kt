package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable

/**
 * Returns a launcher that hands a generated zip archive to the platform's save / share UI. The
 * provided callback receives `true` when the file was successfully persisted, or `false` if the
 * user cancelled or saving failed.
 *
 * The launcher takes the proposed file name (e.g. `"chef-mate-recipes.zip"`) and the raw archive
 * bytes — each platform decides whether to show a "Save As" dialog, a share sheet, or a directly
 * chosen path.
 */
@Composable
expect fun rememberZipSaveLauncher(
    onResult: (Boolean) -> Unit
): (fileName: String, bytes: ByteArray) -> Unit
