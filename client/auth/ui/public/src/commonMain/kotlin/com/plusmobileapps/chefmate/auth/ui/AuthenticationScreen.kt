package com.plusmobileapps.chefmate.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import chefmate.client.auth.ui.public.generated.resources.Res
import chefmate.client.auth.ui.public.generated.resources.auth_button_forgot_password
import chefmate.client.auth.ui.public.generated.resources.auth_button_okay
import chefmate.client.auth.ui.public.generated.resources.auth_button_sign_in
import chefmate.client.auth.ui.public.generated.resources.auth_button_sign_up
import chefmate.client.auth.ui.public.generated.resources.auth_label_confirm_password
import chefmate.client.auth.ui.public.generated.resources.auth_label_email
import chefmate.client.auth.ui.public.generated.resources.auth_label_password
import chefmate.client.auth.ui.public.generated.resources.auth_loading_creating_account
import chefmate.client.auth.ui.public.generated.resources.auth_loading_signing_in
import chefmate.client.auth.ui.public.generated.resources.auth_password_toggle
import chefmate.client.auth.ui.public.generated.resources.auth_screen_title_sign_in
import chefmate.client.auth.ui.public.generated.resources.auth_screen_title_sign_up
import chefmate.client.auth.ui.public.generated.resources.auth_switch_to_sign_in
import chefmate.client.auth.ui.public.generated.resources.auth_switch_to_sign_up
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(
    bloc: AuthenticationBloc,
    modifier: Modifier = Modifier,
) {
    val model by bloc.models.collectAsState()
    val email by bloc.email.collectAsState()
    val password by bloc.password.collectAsState()
    val confirmPassword by bloc.confirmPassword.collectAsState()

    val title =
        if (model.mode == AuthenticationBloc.Model.Mode.SignIn) {
            ResourceString(Res.string.auth_screen_title_sign_in)
        } else {
            ResourceString(Res.string.auth_screen_title_sign_up)
        }

    PlusHeaderContainer(
        modifier = modifier.imePadding(),
        data =
            PlusHeaderData.Child(
                title = title,
                onBackClick = bloc::onBackClicked,
            ),
        scrollEnabled = false,
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                AuthenticationBody(
                    model = model,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    onEmailChanged = bloc::onEmailChanged,
                    onPasswordChanged = bloc::onPasswordChanged,
                    onConfirmPasswordChanged = bloc::onConfirmPasswordChanged,
                    onSubmitClicked = bloc::onSubmitClicked,
                    onToggleMode = bloc::onToggleMode,
                    onForgotPasswordClicked = bloc::onForgotPasswordClicked,
                    onDismissError = bloc::onDismissError,
                )
                if (model.isLoading) {
                    LoadingDialog(
                        message =
                            if (model.mode == AuthenticationBloc.Model.Mode.SignIn) {
                                stringResource(Res.string.auth_loading_signing_in)
                            } else {
                                stringResource(Res.string.auth_loading_creating_account)
                            },
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = bloc::onSubmitClicked,
                text = {
                    Text(
                        if (model.mode == AuthenticationBloc.Model.Mode.SignIn) {
                            stringResource(Res.string.auth_button_sign_in)
                        } else {
                            stringResource(Res.string.auth_button_sign_up)
                        },
                    )
                },
                icon = { Icon(Icons.Default.Check, contentDescription = null) },
                shape = MaterialTheme.shapes.extraLarge,
            )
        },
    )
}

@Composable
private fun LoadingDialog(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(ChefMateTheme.dimens.paddingNormal),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal),
        ) {
            CircularProgressIndicator()
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun AuthenticationBody(
    modifier: Modifier = Modifier,
    model: AuthenticationBloc.Model,
    email: String,
    password: String,
    confirmPassword: String,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onSubmitClicked: () -> Unit,
    onToggleMode: () -> Unit,
    onForgotPasswordClicked: () -> Unit,
    onDismissError: () -> Unit,
) {
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isSignIn = model.mode == AuthenticationBloc.Model.Mode.SignIn

    val error = model.errorMessage

    if (error != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text(error.localized()) },
            confirmButton = {
                Button(onClick = onDismissError) {
                    Text(text = stringResource(Res.string.auth_button_okay))
                }
            },
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = ChefMateTheme.dimens.paddingExtraLarge,
                    start = ChefMateTheme.dimens.paddingNormal,
                    end = ChefMateTheme.dimens.paddingNormal,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmailField(
            email = email,
            onEmailChanged = onEmailChanged,
            onImeAction = {
                passwordFocusRequester.requestFocus()
            },
        )

        Spacer(modifier = Modifier.height(ChefMateTheme.dimens.paddingLarge))

        PasswordField(
            modifier = Modifier.focusRequester(passwordFocusRequester),
            password = password,
            onPasswordChanged = onPasswordChanged,
            imeAction = if (isSignIn) ImeAction.Done else ImeAction.Next,
            onImeAction = {
                if (isSignIn) {
                    keyboardController?.hide()
                    onSubmitClicked()
                } else {
                    confirmPasswordFocusRequester.requestFocus()
                }
            },
        )

        Spacer(modifier = Modifier.height(ChefMateTheme.dimens.paddingNormal))

        AnimatedVisibility(visible = isSignIn) {
            TextButton(onClick = onForgotPasswordClicked) {
                Text(text = stringResource(Res.string.auth_button_forgot_password))
            }
        }

        Spacer(modifier = Modifier.height(ChefMateTheme.dimens.paddingLarge))

        AnimatedVisibility(visible = !isSignIn) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PasswordField(
                    modifier = Modifier.focusRequester(confirmPasswordFocusRequester),
                    password = confirmPassword,
                    onPasswordChanged = onConfirmPasswordChanged,
                    label = stringResource(Res.string.auth_label_confirm_password),
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        keyboardController?.hide()
                        onSubmitClicked()
                    },
                )
                Spacer(modifier = Modifier.height(ChefMateTheme.dimens.paddingLarge))
            }
        }

        AuthenticationSwitcher(
            isSignIn = isSignIn,
            onToggleMode = onToggleMode,
        )

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun AuthenticationSwitcher(
    isSignIn: Boolean,
    onToggleMode: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(ChefMateTheme.dimens.paddingNormal))
        Text(
            text =
                if (isSignIn) {
                    stringResource(Res.string.auth_switch_to_sign_up)
                } else {
                    stringResource(Res.string.auth_switch_to_sign_in)
                },
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(ChefMateTheme.dimens.paddingNormal))
        Button(
            onClick = {
                keyboardController?.hide()
                onToggleMode()
            },
        ) {
            Text(
                text =
                    if (isSignIn) {
                        stringResource(Res.string.auth_button_sign_up)
                    } else {
                        stringResource(Res.string.auth_button_sign_in)
                    },
            )
        }
    }
}

@Composable
fun EmailField(
    modifier: Modifier = Modifier,
    email: String,
    onEmailChanged: (String) -> Unit,
    onImeAction: () -> Unit,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = email,
        onValueChange = onEmailChanged,
        label = { Text(stringResource(Res.string.auth_label_email)) },
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        keyboardActions =
            KeyboardActions(
                onNext = { onImeAction() },
            ),
    )
}

@Composable
fun PasswordField(
    modifier: Modifier = Modifier,
    password: String,
    onPasswordChanged: (String) -> Unit,
    label: String = stringResource(Res.string.auth_label_password),
    imeAction: ImeAction,
    onImeAction: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = password,
        onValueChange = onPasswordChanged,
        label = { Text(label) },
        singleLine = true,
        visualTransformation =
            if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction,
            ),
        keyboardActions =
            KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() },
            ),
        trailingIcon = {
            val image =
                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff

            IconButton(
                onClick = {
                    passwordVisible = !passwordVisible
                },
            ) {
                Icon(
                    imageVector = image,
                    contentDescription = stringResource(Res.string.auth_password_toggle),
                )
            }
        },
    )
}
