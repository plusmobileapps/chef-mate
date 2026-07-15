@file:Suppress("ktlint:standard:filename")
@file:OptIn(ExperimentalForeignApi::class)

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import com.plusmobileapps.chefmate.text.TextData
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIAction
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventEditingChanged
import platform.UIKit.UIControlEventEditingDidBegin
import platform.UIKit.UIControlEventEditingDidEnd
import platform.UIKit.UIControlEventEditingDidEndOnExit
import platform.UIKit.UIKeyboardTypeEmailAddress
import platform.UIKit.UIReturnKeyType
import platform.UIKit.UITextAutocapitalizationType
import platform.UIKit.UITextAutocorrectionType
import platform.UIKit.UITextBorderStyle
import platform.UIKit.UITextContentTypePassword
import platform.UIKit.UITextContentTypeUsername
import platform.UIKit.UITextField
import platform.UIKit.UITextSpellCheckingType

/**
 * iOS renders a real, transparent `UITextField` via interop instead of a Compose text field.
 * Apple's Keychain heuristic only offers the save/update-password prompt (and the QuickType
 * passwords key) when genuine `UITextField`s for the login and password are present on screen —
 * Compose's own text input does not satisfy it yet (JetBrains CMP-5802, targeted 1.13.0).
 *
 * The native field is placed inside [OutlinedTextFieldDefaults.DecorationBox] so it keeps the
 * Material outline, floating label, error, and trailing icon that the rest of the app uses; Compose
 * draws the decoration, the user types into the native field.
 *
 * Background: interop views don't yet composite with a truly transparent background (CMP-3154), so
 * a `clearColor` field renders as an opaque system background (solid white/black) that stands out
 * against the screen. Instead we paint the native field with the resolved Compose [background]
 * color so it blends into the surface it sits on. The color is re-applied on every recompose so it
 * tracks light/dark theme changes. Revisit (and switch back to a clear background) once CMP-3154
 * ships, or if these fields are ever placed over a gradient/image where a solid fill won't match.
 */
@Composable
actual fun PlusAutofillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    fieldType: AutofillFieldType,
    label: String,
    modifier: Modifier,
    error: TextData?,
    passwordVisible: Boolean,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    focusRequester: FocusRequester?,
    trailingIcon: @Composable (() -> Unit)?,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isSecure = fieldType.isSecure(passwordVisible)
    // The Material resting label sits in the center of the field, but the opaque native field
    // composites over it (see class doc) so it's invisible until focus floats it up to the outline.
    // To show a resting hint we set the native field's own placeholder, and clear it while focused
    // so
    // it doesn't collide with the Material label that's floating up onto the border.
    var isFocused by remember { mutableStateOf(false) }
    // The color the field sits on. Painted onto the native field (see class doc) because the
    // interop
    // layer can't render a transparent background, and re-applied in `update` to follow theme
    // changes.
    val fieldBackground = MaterialTheme.colorScheme.background

    // Latest hoisted callbacks, read from inside the once-built native action handlers.
    val callbacks = remember { NativeFieldCallbacks() }
    callbacks.onValueChange = onValueChange
    callbacks.onImeAction = onImeAction

    // The native field is built once; `update` below re-syncs the mutable bits (text, secure entry,
    // return key) whenever Compose recomposes.
    val textField =
        remember(fieldType) {
            UITextField().apply {
                borderStyle = UITextBorderStyle.UITextBorderStyleNone
                // Initial fill; kept in sync with the theme in `update` below.
                backgroundColor = fieldBackground.toUIColor()
                setOpaque(false)
                autocapitalizationType =
                    UITextAutocapitalizationType.UITextAutocapitalizationTypeNone
                autocorrectionType = UITextAutocorrectionType.UITextAutocorrectionTypeNo
                spellCheckingType = UITextSpellCheckingType.UITextSpellCheckingTypeNo

                // The autofill semantics Apple's login-form heuristic keys on.
                when (fieldType) {
                    AutofillFieldType.EMAIL -> {
                        keyboardType = UIKeyboardTypeEmailAddress
                        textContentType = UITextContentTypeUsername
                    }
                    AutofillFieldType.PASSWORD -> textContentType = UITextContentTypePassword
                }

                // Native -> Compose: forward every keystroke (and system autofill) to the caller.
                addAction(
                    UIAction.actionWithHandler { _ -> callbacks.onValueChange(text ?: "") },
                    forControlEvents = UIControlEventEditingChanged,
                )
                // Return key -> submit / advance focus.
                addAction(
                    UIAction.actionWithHandler { _ -> callbacks.onImeAction() },
                    forControlEvents = UIControlEventEditingDidEndOnExit,
                )
                // Drive the Material label float (and the resting placeholder) from native focus.
                addAction(
                    UIAction.actionWithHandler { _ ->
                        isFocused = true
                        val focus = FocusInteraction.Focus()
                        callbacks.focus = focus
                        interactionSource.tryEmit(focus)
                    },
                    forControlEvents = UIControlEventEditingDidBegin,
                )
                addAction(
                    UIAction.actionWithHandler { _ ->
                        isFocused = false
                        callbacks.focus?.let {
                            interactionSource.tryEmit(FocusInteraction.Unfocus(it))
                        }
                        callbacks.focus = null
                    },
                    forControlEvents = UIControlEventEditingDidEnd,
                )
            }
        }

    // DecorationBox draws only the Material chrome and takes no modifier, so apply the caller's
    // modifier here and bridge a Compose focus request (IME "Next" on the previous field) onto the
    // native responder.
    val focusModifier =
        modifier
            .let { base ->
                if (focusRequester != null) base.focusRequester(focusRequester) else base
            }
            .onFocusChanged { if (it.isFocused) textField.becomeFirstResponder() }
            .focusable()

    Box(modifier = focusModifier) {
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            // Masking is handled natively by secureTextEntry, so no Compose transformation applies.
            visualTransformation = VisualTransformation.None,
            innerTextField = {
                UIKitView(
                    factory = { textField },
                    modifier = Modifier.fillMaxWidth().height(24.dp),
                    update = { field ->
                        field.backgroundColor = fieldBackground.toUIColor()
                        // Resting hint (unfocused, empty). Cleared on focus so it doesn't double up
                        // with the Material label floating onto the outline; iOS hides it once the
                        // user types.
                        field.placeholder = if (isFocused) null else label
                        if (field.text != value) field.setText(value)
                        if (field.secureTextEntry != isSecure) {
                            field.secureTextEntry = isSecure
                            // Toggling secureTextEntry can drop the font metrics; re-set to
                            // restore.
                            val currentFont = field.font
                            field.font = null
                            field.font = currentFont
                        }
                        field.returnKeyType =
                            when (imeAction) {
                                ImeAction.Next -> UIReturnKeyType.UIReturnKeyNext
                                ImeAction.Done -> UIReturnKeyType.UIReturnKeyDone
                                ImeAction.Go -> UIReturnKeyType.UIReturnKeyGo
                                ImeAction.Search -> UIReturnKeyType.UIReturnKeySearch
                                ImeAction.Send -> UIReturnKeyType.UIReturnKeySend
                                else -> UIReturnKeyType.UIReturnKeyDefault
                            }
                    },
                )
            },
            enabled = true,
            singleLine = true,
            interactionSource = interactionSource,
            isError = error != null,
            label = { Text(label) },
            trailingIcon = trailingIcon,
            supportingText = error?.let { errorText -> { Text(errorText.localized()) } },
        )
    }
}

/** Bridges a Compose [Color] to the equivalent sRGB [UIColor] for the native field's background. */
private fun Color.toUIColor(): UIColor =
    UIColor(
        red = red.toDouble(),
        green = green.toDouble(),
        blue = blue.toDouble(),
        alpha = alpha.toDouble(),
    )

/** Holds the latest hoisted callbacks + the in-flight focus interaction for the native field. */
private class NativeFieldCallbacks {
    var onValueChange: (String) -> Unit = {}
    var onImeAction: () -> Unit = {}
    var focus: FocusInteraction.Focus? = null
}
