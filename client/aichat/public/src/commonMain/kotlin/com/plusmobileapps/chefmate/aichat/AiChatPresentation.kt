package com.plusmobileapps.chefmate.aichat

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * How the AI chat is currently being presented. The chat screen renders the same content in every
 * case except [SheetPeek], where it collapses to a compact "peek" (just the input over the recipe)
 * so it can be dragged up to full screen.
 *
 * Defaults to [FullScreen] so the standalone chat (opened from the More tab) and all previews
 * render exactly as before — only the recipe-grounded sheet flips this to a sheet value.
 */
enum class AiChatPresentation {
    /** Standalone, full-screen chat (More tab). Header + messages + input. */
    FullScreen,

    /**
     * Bottom sheet at its partially-expanded detent — show only the input to invite a first tap.
     */
    SheetPeek,

    /** Bottom sheet dragged up to full height — same layout as [FullScreen]. */
    SheetExpanded,
}

/** The active presentation for the chat below this point in the tree. */
val LocalAiChatPresentation = compositionLocalOf { AiChatPresentation.FullScreen }

/**
 * Requests that the hosting sheet expand to full height. Wired by the sheet host; a no-op in the
 * full-screen presentation. Static because the callback identity rarely changes.
 */
val LocalAiChatRequestExpand = staticCompositionLocalOf<() -> Unit> { {} }
