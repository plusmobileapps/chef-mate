package com.plusmobileapps.chefmate.auth.ui.otp

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.auth.ui.public.generated.resources.Res
import chefmate.client.auth.ui.public.generated.resources.auth_button_okay
import chefmate.client.auth.ui.public.generated.resources.auth_otp_button_resend
import chefmate.client.auth.ui.public.generated.resources.auth_otp_button_resend_countdown
import chefmate.client.auth.ui.public.generated.resources.auth_otp_button_verify
import chefmate.client.auth.ui.public.generated.resources.auth_otp_instructions
import chefmate.client.auth.ui.public.generated.resources.auth_otp_label_code
import chefmate.client.auth.ui.public.generated.resources.auth_otp_loading_verifying
import chefmate.client.auth.ui.public.generated.resources.auth_otp_screen_title_email_change
import chefmate.client.auth.ui.public.generated.resources.auth_otp_screen_title_passwordless
import chefmate.client.auth.ui.public.generated.resources.auth_otp_screen_title_signup
import com.plusmobileapps.chefmate.auth.data.OtpFlow
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingDialog
import com.plusmobileapps.chefmate.ui.components.PlusTextField
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(bloc: OtpBloc, modifier: Modifier = Modifier) {
    val model by bloc.models.collectAsState()
    val code by bloc.code.collectAsState()

    val title: TextData =
        when (model.flow) {
            OtpFlow.SignUp -> ResourceString(Res.string.auth_otp_screen_title_signup)
            OtpFlow.PasswordlessSignIn ->
                ResourceString(Res.string.auth_otp_screen_title_passwordless)
            OtpFlow.EmailChange -> ResourceString(Res.string.auth_otp_screen_title_email_change)
        }

    PlusHeaderContainer(
        modifier = modifier.imePadding(),
        data = PlusHeaderData.Child(title = title, onBackClick = bloc::onBackClicked),
        scrollEnabled = false,
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                OtpBody(
                    model = model,
                    code = code,
                    onCodeChanged = bloc::onCodeChanged,
                    onVerifyClicked = bloc::onVerifyClicked,
                    onResendClicked = bloc::onResendClicked,
                    onDismissError = bloc::onDismissError,
                )
                if (model.isLoading) {
                    PlusLoadingDialog(
                        message = ResourceString(Res.string.auth_otp_loading_verifying)
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = bloc::onVerifyClicked,
                text = { Text(stringResource(Res.string.auth_otp_button_verify)) },
                icon = { Icon(Icons.Default.Check, contentDescription = null) },
                shape = MaterialTheme.shapes.extraLarge,
            )
        },
    )
}

@Composable
private fun OtpBody(
    model: OtpBloc.Model,
    code: String,
    onCodeChanged: (String) -> Unit,
    onVerifyClicked: () -> Unit,
    onResendClicked: () -> Unit,
    onDismissError: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

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
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = ChefMateTheme.dimens.paddingExtraLarge,
                    start = ChefMateTheme.dimens.paddingNormal,
                    end = ChefMateTheme.dimens.paddingNormal,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text =
                PhraseModel(Res.string.auth_otp_instructions, "email" to FixedString(model.email))
                    .localized(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(ChefMateTheme.dimens.paddingLarge))

        PlusTextField(
            modifier = Modifier.fillMaxWidth(),
            value = code,
            onValueChange = onCodeChanged,
            label = { Text(stringResource(Res.string.auth_otp_label_code)) },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        onVerifyClicked()
                    }
                ),
        )

        Spacer(modifier = Modifier.height(ChefMateTheme.dimens.paddingNormal))

        val info = model.infoMessage
        if (info != null) {
            Text(
                text = info.localized(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(ChefMateTheme.dimens.paddingNormal))
        }

        val countdown = model.resendCountdownSeconds
        TextButton(onClick = onResendClicked, enabled = countdown == 0 && !model.isLoading) {
            Text(
                text =
                    if (countdown > 0) {
                        PhraseModel(
                                Res.string.auth_otp_button_resend_countdown,
                                "seconds" to FixedString(countdown.toString()),
                            )
                            .localized()
                    } else {
                        stringResource(Res.string.auth_otp_button_resend)
                    }
            )
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}
