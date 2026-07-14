@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import com.plusmobileapps.chefmate.text.TextData

// Desktop has no OS credential-autofill integration to satisfy, so delegate to the shared fallback
// and behave exactly like the standard PlusTextField.
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
) =
    FallbackAutofillTextField(
        value = value,
        onValueChange = onValueChange,
        fieldType = fieldType,
        label = label,
        modifier = modifier,
        error = error,
        passwordVisible = passwordVisible,
        imeAction = imeAction,
        onImeAction = onImeAction,
        focusRequester = focusRequester,
        trailingIcon = trailingIcon,
    )
