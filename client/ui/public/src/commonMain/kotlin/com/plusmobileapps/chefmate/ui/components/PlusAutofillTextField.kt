package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.plusmobileapps.chefmate.text.TextData

/**
 * Which credential a [PlusAutofillTextField] represents. Each platform maps this to the right
 * autofill semantics (iOS `textContentType`, Android autofill hints) and keyboard so the system
 * offers to fill/save the account login.
 */
enum class AutofillFieldType {
    /**
     * The account identifier. Users type their email, but it is hinted as the login "username" so
     * password managers classify it unambiguously as a login field.
     */
    EMAIL,
    /**
     * A login password. Hinted so password managers offer to fill the account's saved password.
     * Used for both sign in and sign up: a plain password hint (rather than a "new password" one)
     * is what lets third-party managers surface an existing credential on the sign-up form too —
     * iOS only offers its generate-a-new-password flow through iCloud Keychain, so a new-password
     * hint shows nothing when a third-party manager is the AutoFill provider.
     */
    PASSWORD,
}

/**
 * A credential text field for login/signup forms.
 *
 * This exists as a separate component from [PlusTextField] because the iOS Keychain save/update
 * prompt requires *real* `UITextField`s on screen (see JetBrains CMP-5802). The iOS actual
 * therefore renders a native `UITextField` via interop, which carries tradeoffs (native<->Compose
 * text sync, the CMP-3154 transparent-background caveat) that we deliberately keep out of the
 * general-purpose [PlusTextField]. Every non-iOS platform delegates straight back to
 * [PlusTextField] via [FallbackAutofillTextField], so only iOS auth screens take the native path.
 *
 * The caller owns [passwordVisible] and supplies the visibility [trailingIcon]; the component only
 * consumes [passwordVisible] to decide whether the value is masked (or, on iOS, secure entry).
 *
 * Remove this component and its iOS actual once CMP-5802 ships (targeted Compose Multiplatform
 * 1.13.0) and fold the callers back onto [PlusTextField].
 */
@Composable
expect fun PlusAutofillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    fieldType: AutofillFieldType,
    label: String,
    modifier: Modifier = Modifier,
    error: TextData? = null,
    passwordVisible: Boolean = false,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {},
    focusRequester: FocusRequester? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
)

/** Whether the field's contents should be obscured (any password field that isn't revealed). */
internal fun AutofillFieldType.isSecure(passwordVisible: Boolean): Boolean =
    this != AutofillFieldType.EMAIL && !passwordVisible

internal val AutofillFieldType.composeContentType: ContentType
    get() =
        when (this) {
            // This is the account's login identifier, which password managers store as the
            // "username" even though users type their email into it. Hinting plain Username (rather
            // than also EmailAddress) classifies it unambiguously as a login field, so managers
            // like
            // Bitwarden proactively offer the on-focus inline suggestion instead of only filling it
            // on demand.
            AutofillFieldType.EMAIL -> ContentType.Username
            AutofillFieldType.PASSWORD -> ContentType.Password
        }

internal val AutofillFieldType.keyboardType: KeyboardType
    get() =
        when (this) {
            AutofillFieldType.EMAIL -> KeyboardType.Email
            AutofillFieldType.PASSWORD -> KeyboardType.Password
        }

// Masks secure input with bullets. Replacing 1:1 keeps the caret mapping identity, so the cursor
// stays put. Mirrors the masking PlusTextField callers previously applied inline.
private val PasswordMaskTransformation = OutputTransformation {
    replace(0, length, "•".repeat(length))
}

/**
 * Shared non-iOS implementation: the existing [PlusTextField] with the credential autofill hints
 * applied. Android's Compose autofill already fills/saves logins here, and desktop is unaffected,
 * so these platforms need nothing more than the hint. Android and JVM `actual`s both delegate here.
 */
@Composable
internal fun FallbackAutofillTextField(
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
    PlusTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        error = error,
        singleLine = true,
        focusRequester = focusRequester,
        outputTransformation =
            if (fieldType.isSecure(passwordVisible)) PasswordMaskTransformation else null,
        keyboardOptions =
            KeyboardOptions(keyboardType = fieldType.keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onImeAction() }, onDone = { onImeAction() }),
        contentType = fieldType.composeContentType,
        trailingIcon = trailingIcon,
    )
}
