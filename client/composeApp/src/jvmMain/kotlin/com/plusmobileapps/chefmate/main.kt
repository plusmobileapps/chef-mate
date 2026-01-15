@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.parseSessionFromUrl
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    // Initialize Napier logging for JVM
    Napier.base(DebugAntilog())
    // Only initialize the lifecycle outside the application block
    val lifecycle = LifecycleRegistry()
    val backDispatcher = BackDispatcher()
    val appComponent = JvmApplicationComponent::class.create()
    val supabaseClient = appComponent.supabaseClient
    val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Handle a deep link URI by parsing tokens from the fragment and importing the session.
     * The URI format is: chefmate://auth/callback#access_token=...&refresh_token=...&expires_in=...&token_type=bearer
     */
    fun handleDeepLinkUri(uriString: String) {
        scope.launch {
            supabaseClient.handleDeeplinks(uriString)
        }
    }

    // Handle deep link from command line args (Linux/Windows)
    // When the OS opens a deep link, it launches the app with the URI as an argument
    args.firstOrNull()?.let { uri ->
        if (uri.startsWith("chefmate://")) {
            DeepLinkHandler.onNewUri(uri)
        }
    }

    // Handle macOS deep links via Desktop API
    // Set this up BEFORE the application block to catch URIs that arrive during app launch
    // macOS sends URIs through the Desktop API instead of command line args
    if (Desktop.isDesktopSupported()) {
        try {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.APP_OPEN_URI)) {
                desktop.setOpenURIHandler { event ->
                    val uri = event.uri.toString()
                    DeepLinkHandler.onNewUri(uri)
                }
            }
        } catch (e: Exception) {
            Napier.e("Failed to set up macOS URI handler", e)
        }
    }

    application {
        // Set up DeepLinkHandler listener to forward URIs to our handler
        DeepLinkHandler.listener = { uri ->
            handleDeepLinkUri(uri)
        }

        // Initialize the DefaultComponentContext inside the application block
        // to ensure it runs on the main thread
        val rootBloc =
            buildRoot(
                componentContext =
                    DefaultComponentContext(
                        lifecycle = lifecycle,
                        backHandler = backDispatcher,
                    ),
                applicationComponent = appComponent,
            )

        Window(
            onCloseRequest = ::exitApplication,
            title = "Chef Mate",
            onKeyEvent = { event ->
                if ((event.key == Key.Escape) && (event.type == KeyEventType.KeyUp)) {
                    backDispatcher.back()
                } else {
                    false
                }
            },
        ) {
            App(rootBloc = rootBloc)
        }
    }
}

@OptIn(ExperimentalTime::class)
suspend fun SupabaseClient.handleDeeplinks(
    url: String,
    onSessionSuccess: (UserSession) -> Unit = {},
    onError: (Throwable) -> Unit = {},
) = withContext(Dispatchers.IO) {
    try {
        val session: UserSession = auth.parseSessionFromUrl(url)
        val user = auth.retrieveUser(session.accessToken)
        val updatedSession = session.copy(user = user)
        auth.importSession(updatedSession)
        onSessionSuccess(updatedSession)
    } catch (e: Exception) {
        onError(e)
    }
}
