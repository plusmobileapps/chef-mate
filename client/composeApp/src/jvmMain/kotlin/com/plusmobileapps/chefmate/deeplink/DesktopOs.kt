package com.plusmobileapps.chefmate.deeplink

/** The three desktop targets, used to branch the OS-specific deep-link integration. */
internal enum class DesktopOs {
    MACOS,
    WINDOWS,
    LINUX,
}

internal fun desktopOs(): DesktopOs {
    val os = System.getProperty("os.name").orEmpty().lowercase()
    return when {
        os.contains("mac") || os.contains("darwin") -> DesktopOs.MACOS
        os.contains("win") -> DesktopOs.WINDOWS
        else -> DesktopOs.LINUX
    }
}
