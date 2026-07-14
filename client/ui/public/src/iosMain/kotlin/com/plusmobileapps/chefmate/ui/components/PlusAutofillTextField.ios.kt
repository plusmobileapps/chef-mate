@file:Suppress("ktlint:standard:filename")
@file:OptIn(ExperimentalForeignApi::class)

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import platform.UIKit.UITextContentTypeNewPassword
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
 * The native field is made transparent and placed inside [OutlinedTextFieldDefaults.DecorationBox]
 * so it keeps the Material outline, floating label, error, and trailing icon that the rest of the
 * app uses; Compose draws the decoration, the user types into the native field.
 *
 * Known caveat: interop views don't yet composite with a truly transparent background in every case
 * (CMP-3154). Our auth fields sit on the surface color, so a clear background reads correctly;
 * revisit if these fields are ever placed over a gradient/image.
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
                backgroundColor = UIColor.clearColor
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
                    AutofillFieldType.NEW_PASSWORD -> textContentType = UITextContentTypeNewPassword
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
                // Drive the Material label float from native focus changes.
                addAction(
                    UIAction.actionWithHandler { _ ->
                        val focus = FocusInteraction.Focus()
                        callbacks.focus = focus
                        interactionSource.tryEmit(focus)
                    },
                    forControlEvents = UIControlEventEditingDidBegin,
                )
                addAction(
                    UIAction.actionWithHandler { _ ->
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

/** Holds the latest hoisted callbacks + the in-flight focus interaction for the native field. */
private class NativeFieldCallbacks {
    var onValueChange: (String) -> Unit = {}
    var onImeAction: () -> Unit = {}
    var focus: FocusInteraction.Focus? = null
}
