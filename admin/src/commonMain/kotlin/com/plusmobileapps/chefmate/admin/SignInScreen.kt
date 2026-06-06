package com.plusmobileapps.chefmate.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private enum class SignInStep {
    EnterEmail,
    EnterCode,
}

@Composable
fun SignInScreen(auth: AdminAuth) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(SignInStep.EnterEmail) }
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("ChefMate feature flags", style = MaterialTheme.typography.headlineSmall)

            when (step) {
                SignInStep.EnterEmail -> {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Admin email") },
                        singleLine = true,
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    Button(
                        onClick = {
                            error = null
                            loading = true
                            scope.launch {
                                auth
                                    .sendOtp(email.trim())
                                    .onSuccess { step = SignInStep.EnterCode }
                                    .onFailure { error = it.message ?: "Could not send code" }
                                loading = false
                            }
                        },
                        enabled = !loading && email.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Send sign-in code")
                    }
                }
                SignInStep.EnterCode -> {
                    Text("Enter the code emailed to $email")
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("6-digit code") },
                        singleLine = true,
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Button(
                        onClick = {
                            error = null
                            loading = true
                            scope.launch {
                                auth.verifyOtp(email.trim(), code.trim()).onFailure {
                                    error = it.message ?: "Invalid code"
                                }
                                // On success the session flips and AdminRoot swaps to the
                                // dashboard.
                                loading = false
                            }
                        },
                        enabled = !loading && code.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Verify & sign in")
                    }
                    TextButton(
                        onClick = {
                            step = SignInStep.EnterEmail
                            code = ""
                            error = null
                        },
                        enabled = !loading,
                    ) {
                        Text("Use a different email")
                    }
                }
            }

            if (loading) CircularProgressIndicator()
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
