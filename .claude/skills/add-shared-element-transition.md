# add-shared-element-transition

Add a Compose shared element transition between two screens in this Decompose-based KMP app.

## Required input

Ask the user (if not provided):

1. **Source composable** — where the element is rendered before the transition (file, callsite).
2. **Target composable** — where the element is rendered after (file, callsite).
3. **What identifies the element** — typically a domain id (e.g. `recipe.id`, `groceryItem.id`) used to build the shared key.

## Decide the integration point

Find the lowest navigation BLoC whose `ChildStack` contains both source and target as descendants. That's where `SharedTransitionLayout` + `AnimatedContent` must live.

- **Same `ChildStack` (siblings)** — e.g. `BrowserRootBloc`'s Landing/EditQuery/Browser. See `client/browser/public/.../BrowserRootScreen.kt` for the canonical example. Easy: replace its render with `SharedTransitionLayout { AnimatedContent { ... } }` and pass scopes as parameters.
- **Different `ChildStack`s** — e.g. recipe list (under `BottomNavigation` child) → recipe detail (under `RecipeRoot` child). The integration point is `RootScreen.kt`. See it for the reference implementation. The deep child (list image) consumes the scope via `CompositionLocal`, not parameters.

If integrating at `RootScreen` (or any layer that already uses Decompose's `Children` + `predictiveBackAnimation`), warn the user that **root-level predictive back gesture preview is lost** — `AnimatedContent` cannot drive a gesture-tracked animation. Predictive back inside descendant ChildStacks is unaffected.

## The pattern

### 1. CompositionLocals

Already exist at `client/ui/public/src/commonMain/kotlin/com/plusmobileapps/chefmate/ui/SharedTransitionScopes.kt`:

```kotlin
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
```

Reuse — do not redefine.

### 2. The navigation screen (integration point)

```kotlin
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SomeRootScreen(bloc: SomeRootBloc, modifier: Modifier = Modifier) {
    val stack by bloc.routerState.subscribeAsState()
    val saveableStateHolder = rememberSaveableStateHolder()
    val previousKeys = remember { mutableSetOf<String>() }

    DisposableEffect(stack) {
        val currentKeys = stack.items.mapTo(HashSet()) { it.saveableKey() }
        previousKeys.forEach { if (it !in currentKeys) saveableStateHolder.removeState(it) }
        previousKeys.clear()
        previousKeys.addAll(currentKeys)
        onDispose {}
    }

    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = stack.active,
            contentKey = { it.key },
            transitionSpec = { /* slide / fade — see RootScreen.kt for direction-aware spec */ },
            label = "some-root",
        ) { activeChild ->
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this@SharedTransitionLayout,
                LocalAnimatedVisibilityScope provides this,
            ) {
                saveableStateHolder.SaveableStateProvider(activeChild.saveableKey()) {
                    when (val child = activeChild.instance) {
                        // ... when branches per Child type
                    }
                }
            }
        }
    }
}

private fun com.arkivanov.decompose.Child<*, *>.saveableKey(): String =
    "${configuration::class.simpleName}_${key.hashCode()}"
```

### 3. The shared component

Add an optional `sharedElementKey: String?` param. Read CompositionLocals; apply `Modifier.sharedElement` only when both key and scopes are non-null. Pattern (see `RecipeImage.kt`):

```kotlin
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MyComponent(/* existing params */, sharedElementKey: String? = null) {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalAnimatedVisibilityScope.current
    val sharedModifier =
        if (sharedElementKey != null && sharedScope != null && animatedScope != null) {
            with(sharedScope) {
                Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = sharedElementKey),
                    animatedVisibilityScope = animatedScope,
                )
            }
        } else Modifier

    // Apply sharedModifier BEFORE clip / background — it must wrap the visual bounds.
    Box(modifier = modifier.then(sharedModifier).clip(...)) { ... }
}
```

### 4. Callsites

Source and target both pass the same key, derived from the domain id:

```kotlin
MyComponent(..., sharedElementKey = "my-thing-${item.id}")
```

The key must be unique per logical element across the whole app. Prefix with the component/feature so keys from different shared transitions can't collide.

## Gotchas — do not skip

1. **Android Bundle crash on `SaveableStateProvider`** — Decompose's `Child.key` is the `Configuration` instance, which Android can't put in a `Bundle`. Always pass a `String`. Use the `saveableKey()` extension above.
2. **Don't use `@InternalDecomposeApi`** — `keyHashString()` is internal. Define your own `saveableKey()` per the snippet above.
3. **`contentKey = { it.key }` is fine** — `AnimatedContent.contentKey` only does equality comparison, no Bundle storage.
4. **`SaveableStateHolder` retention** — Without the `DisposableEffect` cleanup, state for popped children leaks until process death. Always pair with the `previousKeys` tracker.
5. **CompositionLocal default must be nullable** — `compositionLocalOf<SharedTransitionScope?> { null }`. Components must gracefully no-op when scopes are absent (so they still render correctly outside a `SharedTransitionLayout`, e.g. in previews).
6. **`Modifier.sharedElement` placement** — apply it before any `clip` / `background` so the shared bounds match the visible bounds. If clip is applied first, the shared element animates the unclipped rect.
7. **Replicating `slide()` / `verticalSlide()`** — Decompose's animator is direction-aware. In `transitionSpec`, infer direction from `stack.items.indexOfFirst { it.key == initialState.key }` vs `targetState.key`. If `initialIndex < 0` (popped), treat as back. See `RootScreen.kt` for the full spec.
8. **Preview support** — `RecipeImage`-style components rendered in a `@Preview` won't have `LocalSharedTransitionScope` set; the null-check above handles this. Don't wrap previews in `SharedTransitionLayout` unless the preview is specifically for the transition.

## Verification checklist

Before reporting done:

1. `./gradlew ktfmtFormat`
2. `./gradlew :client:composeApp:compileKotlinJvm` — must pass.
3. **Run on Android.** A passing JVM compile does not catch the Bundle crash on `SaveableStateProvider`. The bug appears at runtime on first navigation. If the user can't run it themselves, say so explicitly — don't claim success.
4. Visually verify the morph: source → target should show the element growing/shrinking smoothly, not snapping.

## Reference implementations in this repo

- `client/browser/public/.../BrowserRootScreen.kt` — same-stack siblings, scopes via parameters.
- `client/root/public/.../RootScreen.kt` — cross-stack via CompositionLocals, direction-aware slide, `saveableKey()`.
- `client/ui/public/.../components/RecipeImage.kt` — opt-in `sharedElementKey` parameter pattern.
- `client/ui/public/.../SharedTransitionScopes.kt` — CompositionLocals.
