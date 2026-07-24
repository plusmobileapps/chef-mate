package com.plusmobileapps.chefmate.aichat

import androidx.compose.runtime.compositionLocalOf

/**
 * How the AI chat is currently being presented. The chat body is identical in both cases; only the
 * app bar's leading control differs.
 *
 * Defaults to [FullScreen] so the standalone chat (opened from the More tab) and all previews
 * render with a back arrow — the recipe-grounded modal flips this to [SheetExpanded] for a close
 * button.
 */
enum class AiChatPresentation {
    /** Standalone, full-screen chat (More tab), with a back arrow in the app bar. */
    FullScreen,

    /**
     * The recipe-grounded chat, opened full-screen over a recipe or Cook Mode. Same layout as
     * [FullScreen] but the app bar carries a close (X) instead of a back arrow.
     */
    SheetExpanded,
}

/** The active presentation for the chat below this point in the tree. */
val LocalAiChatPresentation = compositionLocalOf { AiChatPresentation.FullScreen }
