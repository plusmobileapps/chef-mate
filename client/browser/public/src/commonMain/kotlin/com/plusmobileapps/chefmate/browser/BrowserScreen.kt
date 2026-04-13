package com.plusmobileapps.chefmate.browser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import chefmate.client.browser.public.generated.resources.Res
import chefmate.client.browser.public.generated.resources.browser_address_hint
import chefmate.client.browser.public.generated.resources.browser_extract_recipe
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import org.jetbrains.compose.resources.stringResource

@Composable
fun BrowserScreen(
    bloc: BrowserBloc,
    modifier: Modifier = Modifier,
) {
    val viewState by bloc.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val webViewState = rememberWebViewState(url = viewState.currentUrl)
    val webViewNavigator = rememberWebViewNavigator()

    LaunchedEffect(webViewState.lastLoadedUrl) {
        webViewState.lastLoadedUrl?.let { url ->
            bloc.onUrlLoadedInWebView(url)
        }
    }

    val message = viewState.extractionMessage
    val messageText = message?.localized()
    LaunchedEffect(message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            bloc.onDismissMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (viewState.currentUrl.isNotBlank()) {
                FloatingActionButton(
                    onClick = bloc::onExtractRecipe,
                ) {
                    if (viewState.isExtracting) {
                        CircularProgressIndicator()
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = stringResource(Res.string.browser_extract_recipe),
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            AddressBar(
                url = viewState.addressBarText,
                onUrlChanged = bloc::onUrlChanged,
                onNavigate = {
                    bloc.onNavigate()
                    webViewNavigator.loadUrl(viewState.addressBarText.ensureHttps())
                },
            )
            WebView(
                state = webViewState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                navigator = webViewNavigator,
            )
        }
    }
}

@Composable
private fun AddressBar(
    url: String,
    onUrlChanged: (String) -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(Res.string.browser_address_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(
                onGo = { onNavigate() },
            ),
        )
        IconButton(onClick = onNavigate) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(Res.string.browser_extract_recipe),
            )
        }
    }
}

private fun String.ensureHttps(): String =
    if (!startsWith("http://") && !startsWith("https://")) {
        "https://$this"
    } else {
        this
    }
