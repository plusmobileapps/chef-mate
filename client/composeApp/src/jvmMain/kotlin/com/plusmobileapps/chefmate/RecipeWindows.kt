package com.plusmobileapps.chefmate

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootBloc
import com.plusmobileapps.chefmate.toast.LocalToastService
import com.plusmobileapps.chefmate.toast.ToastService
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * One recipe torn off into its own OS window.
 *
 * It owns a private [LifecycleRegistry], [BackDispatcher] and bloc tree because Decompose state is
 * per-tree — the main window's are already in use and cannot be shared. The DI graph *is* shared
 * (it is app-scoped), so both windows read the same repositories over the same SQLDelight flows,
 * and an edit in one shows up live in the other with no plumbing between them.
 */
class RecipeWindow(
    val recipeId: Long,
    val title: String,
    applicationComponent: ApplicationComponent,
    private val onOutput: (RecipeRootBloc.Output) -> Unit,
    private val onClose: (RecipeWindow) -> Unit,
) {
    private val lifecycle = LifecycleRegistry()

    val backDispatcher = BackDispatcher()

    /**
     * Bumped when the user re-opens a recipe that already has a window, so the existing window can
     * come forward instead of a duplicate appearing behind it.
     */
    var focusRequests by mutableStateOf(0)
        private set

    val bloc: RecipeRootBloc =
        applicationComponent.recipeRootBlocFactory.create(
            context =
                DefaultBlocContext(
                    DefaultComponentContext(lifecycle = lifecycle, backHandler = backDispatcher)
                ),
            props = RecipeRootBloc.Props.Detail(recipeId),
            output = { output ->
                // Finished is this window saying it is done; everything else leaves the recipe
                // stack entirely and only the main window can service it.
                if (output == RecipeRootBloc.Output.Finished) close() else onOutput(output)
            },
        )

    init {
        lifecycle.resume()
    }

    fun requestFocus() {
        focusRequests++
    }

    fun close() {
        lifecycle.destroy()
        onClose(this)
    }
}

/**
 * The set of open recipe windows. Held for the life of the process (not inside the composition) so
 * that the `application { }` block recomposing never disturbs a window the user has open.
 */
class RecipeWindowManager(private val applicationComponent: ApplicationComponent) {
    private val _windows = mutableStateListOf<RecipeWindow>()
    val windows: List<RecipeWindow>
        get() = _windows

    private val _outputs =
        MutableSharedFlow<RecipeRootBloc.Output>(replay = 0, extraBufferCapacity = 8)

    /**
     * Outputs raised in a detached window that need the main window's navigation stack. The main
     * window collects these, routes them through its `RootBloc`, and comes to the front.
     */
    val outputs: SharedFlow<RecipeRootBloc.Output> = _outputs.asSharedFlow()

    fun open(recipeId: Long, title: String) {
        val existing = _windows.firstOrNull { it.recipeId == recipeId }
        if (existing != null) {
            existing.requestFocus()
            return
        }
        _windows +=
            RecipeWindow(
                recipeId = recipeId,
                title = title,
                applicationComponent = applicationComponent,
                onOutput = { _outputs.tryEmit(it) },
                onClose = { _windows -= it },
            )
    }

    /** Tears down every open window. Called when the app is quitting. */
    fun closeAll() {
        _windows.toList().forEach { it.close() }
    }
}

@Composable
fun RecipeWindows(manager: RecipeWindowManager, toastService: ToastService) {
    for (recipeWindow in manager.windows) {
        key(recipeWindow) {
            val windowState = rememberWindowState(size = DpSize(900.dp, 780.dp))
            Window(
                onCloseRequest = recipeWindow::close,
                state = windowState,
                title = recipeWindow.title,
                icon = painterResource("app-icon.png"),
                onKeyEvent = { event ->
                    if ((event.key == Key.Escape) && (event.type == KeyEventType.KeyUp)) {
                        recipeWindow.backDispatcher.back()
                    } else {
                        false
                    }
                },
            ) {
                LaunchedEffect(recipeWindow.focusRequests) {
                    if (recipeWindow.focusRequests > 0) {
                        window.toFront()
                        window.requestFocus()
                    }
                }
                // The one global ToastScaffold lives in the main window and drains the app-scoped
                // toast queue; a second host here would race it for the same messages and show
                // each one twice. Provide the service on its own so composables that read it
                // (RecipeDetailScreen does) still resolve — their toasts surface in the main
                // window.
                CompositionLocalProvider(LocalToastService provides toastService) {
                    ChefMateTheme {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            recipeWindow.bloc.Content(Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}
