package com.plusmobileapps.chefmate.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A renderable screen — a Bloc that knows how to draw itself.
 *
 * Implement this on a Bloc interface so navigation containers (Decompose `ChildStack`s, modal
 * hosts, etc.) can render any child uniformly by calling `bloc.Content()` instead of dispatching
 * with a `when` over a sealed `Child` hierarchy.
 *
 * ### Where to put the `Content` implementation
 * Prefer giving the interface a **default `Content` implementation** in the feature's `public`
 * module that delegates to the screen composable. This keeps the `impl` Bloc free of rendering
 * concerns and means every implementation (production, preview fake, test stub) renders the same
 * screen without overriding `Content`.
 *
 * ### Example
 *
 * ```kotlin
 * // client/<feature>/public/.../FooBloc.kt
 * interface FooBloc : ComposeScreen {
 *     val state: StateFlow<Model>
 *     fun onClicked()
 *
 *     @Composable
 *     override fun Content(modifier: Modifier) {
 *         FooScreen(bloc = this, modifier = modifier)
 *     }
 *
 *     data class Model(...)
 * }
 *
 * // Navigation host renders any child without a when statement:
 * Children(stack = routerState) { child -> child.instance.bloc.Content() }
 * ```
 *
 * The screen composable (`FooScreen`) lives alongside the interface in the `public` module so the
 * default implementation can reference it.
 */
interface ComposeScreen {
    /** Renders this screen. Implementations should respect [modifier] for layout-driven sizing. */
    @Composable fun Content(modifier: Modifier)
}

/** Convenience overload — renders [ComposeScreen.Content] with [Modifier] as the default. */
@Composable fun ComposeScreen.Content() = Content(Modifier)
