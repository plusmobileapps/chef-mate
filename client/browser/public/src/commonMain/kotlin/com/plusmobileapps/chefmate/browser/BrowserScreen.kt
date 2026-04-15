package com.plusmobileapps.chefmate.browser

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import chefmate.client.browser.public.generated.resources.tab_browser
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import org.jetbrains.compose.resources.stringResource

@Composable
fun BrowserScreen(bloc: BrowserBloc, modifier: Modifier = Modifier) {
    val viewState by bloc.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val message = viewState.extractionMessage
    val messageText = message?.localized()
    LaunchedEffect(message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            bloc.onDismissMessage()
        }
    }

    PlusNavContainer(
        modifier = modifier.fillMaxSize(),
        data = PlusHeaderData.Parent(title = Res.string.tab_browser.asTextData()),
        scrollEnabled = false,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = {
            AddressBar(
                url = viewState.addressBarText,
                onUrlChanged = bloc::onUrlChanged,
                onNavigate = bloc::onNavigate,
                showExtract = viewState.currentUrl.isNotBlank(),
                isExtracting = viewState.isExtracting,
                onExtractRecipe = bloc::onExtractRecipe,
            )
            PlatformWebView(
                url = viewState.navigateUrl,
                onUrlLoaded = bloc::onUrlLoadedInWebView,
                instanceKeeper = bloc.instanceKeeper,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        },
    )
}

@Composable
private fun AddressBar(
    url: String,
    onUrlChanged: (String) -> Unit,
    onNavigate: () -> Unit,
    showExtract: Boolean,
    isExtracting: Boolean,
    onExtractRecipe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(Res.string.browser_address_hint)) },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onNavigate() }),
        )
        IconButton(onClick = onNavigate) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(Res.string.browser_extract_recipe),
            )
        }
        if (showExtract) {
            IconButton(onClick = onExtractRecipe) {
                if (isExtracting) {
                    CircularProgressIndicator()
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = stringResource(Res.string.browser_extract_recipe),
                    )
                }
            }
        }
    }
}
