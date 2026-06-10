package com.plusmobileapps.chefmate.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-wide preference for how the recipe text editors (ingredients, directions, description)
 * behave: a WYSIWYG rich-text editor ([richTextMode] = true) or raw markdown with a preview toggle
 * (false). Persisted via multiplatform-settings so the choice sticks across launches and applies
 * everywhere. Defaults to rich text — the more approachable mode, especially on mobile.
 */
@Inject
@SingleIn(AppScope::class)
class MarkdownEditorModeRepository(settings: Settings) {
    private var pref by settings.boolean(KEY, defaultValue = true)

    private val _richTextMode = MutableStateFlow(pref)
    val richTextMode: StateFlow<Boolean> = _richTextMode.asStateFlow()

    fun setRichTextMode(value: Boolean) {
        pref = value
        _richTextMode.value = value
    }

    companion object {
        private const val KEY = "markdown_editor_rich_text_mode"
    }
}
