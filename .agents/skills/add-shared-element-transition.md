# add-shared-element-transition

Add a Compose shared element transition in this Decompose-based KMP app.

## Hard rule: never at the root

**Root predictive-back is the priority.** Decompose's `Children` + `predictiveBackAnimation`
drives the system back gesture across the whole app, and an `AnimatedContent` cannot drive a
gesture-tracked animation. So:

- **Never** wrap root navigation (`RootScreen.kt`) in `SharedTransitionLayout` + `AnimatedContent`.
  That combination was removed for exactly this reason — it disabled predictive back app-wide.
- **Never** hoist `LocalSharedTransitionScope` to the root.
- A shared element must therefore live **entirely within a single self-contained morph** that owns
  its own navigation and does not cross the root `ChildStack`.

If the only place that contains both source and target is the root stack (e.g. recipe list under
`BottomNavigation` → recipe detail under `RecipeRoot`), then a shared element is **not allowed** —
use a normal slide/fade transition instead. Don't trade predictive back for a morph.

## The two valid shapes

Both are self-contained: the owning composable provides its **own local** `SharedTransitionLayout`,
so predictive back everywhere else is unaffected.

1. **Same-stack siblings** — source and target are children of the *same* feature nav BLoC's
   `ChildStack`. Replace that screen's render with `SharedTransitionLayout { AnimatedContent { ... } }`
   and pass the scopes down. Canonical example: `client/browser/public/.../BrowserRootScreen.kt`
   (the address-bar morph between Landing / EditQuery / Browser).

2. **In-screen morph driven by a `ChildSlot`** — one screen toggles between two renderings of itself,
   driven by a Decompose `ChildSlot` on its BLoC. Canonical example: the recipe-detail image ↔
   full-screen image morph in `client/recipe/core/impl/.../detail/ui/RecipeDetailScreen.kt`.

### Reference: the recipe-detail in-screen morph

This is the pattern to copy for a "tap a thing, it expands to full screen" morph.

- **BLoC owns the toggle.** `RecipeDetailBloc` exposes `fullImageSlot: Value<ChildSlot<*, FullImage>>`.
  `onImageClicked()` activates the slot; `onCloseFullImage()` dismisses it. The slot — not local
  `remember` state — is the source of truth, so back/dismiss flow through normal Decompose navigation.
- **The screen provides a local scope and crossfades on the slot:**

```kotlin
val fullImageSlot by bloc.fullImageSlot.subscribeAsState()
val fullImageActive = fullImageSlot.child?.instance as? RecipeDetailBloc.FullImage.Active

// Local to THIS screen — not the root — so the root stack keeps predictive back.
SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
    CompositionLocalProvider(LocalSharedTransitionScope provides this@SharedTransitionLayout) {
        AnimatedContent(
            targetState = fullImageActive,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(250)) },
            label = "recipe-detail-full-image",
            modifier = Modifier.fillMaxSize(),
        ) { fullImage ->
            if (fullImage != null) {
                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                    RecipeImageFullScreenScreen(/* registers key via sharedElementBy(...) */)
                }
            } else {
                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                    RecipeDetailBody(/* body RecipeImage registers the SAME key */)
                }
            }
        }
    }
}
```

Both branches provide the `AnimatedContent`'s own AVS as `LocalAnimatedVisibilityScope`, and both
ends apply `Modifier.sharedElementBy("recipe-image-fullscreen-$id")` (same key). Because the two
content slots transition in opposite directions within one local `SharedTransitionLayout`, the image
morphs.

> Note: `RecipeImage` currently takes a `secondarySharedElementKey` and the body branch wires it
> through `LocalSecondaryAnimatedVisibilityScope`. That two-scope indirection is a **leftover** from
> when the body image *also* held a root-level (list → detail) registration. The root transition is
> gone, so a fresh in-screen morph needs only **one** registration via the primary
> `sharedElementBy(key)` + `LocalAnimatedVisibilityScope` — don't replicate the secondary scope.

## The shared component

Use the helpers in `client/ui/public/.../SharedTransitionScopes.kt` — do not redefine them:

```kotlin
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@Composable fun Modifier.sharedElementBy(key: String?): Modifier  // reads both locals
```

Add an optional `String?` key param to the component and apply the helper. It no-ops when the key or
either scope is null, so the component still renders fine outside a `SharedTransitionLayout` (e.g. in
previews).

```kotlin
@Composable
fun MyComponent(/* existing params */, sharedElementKey: String? = null) {
    Box(modifier = modifier.sharedElementBy(sharedElementKey).clip(...)) { ... }
}
```

Source and target both pass the same key, derived from a domain id and prefixed per feature so keys
can't collide app-wide: `sharedElementKey = "recipe-image-fullscreen-${recipe.id}"`.

## Gotchas — do not skip

1. **Apply the shared modifier before `clip` / `background`** so the shared bounds match the visible
   bounds. If clip comes first, the element animates the unclipped rect.
2. **CompositionLocal default must be nullable** (`compositionLocalOf<…?> { null }`) and the component
   must gracefully no-op when scopes are absent — required for previews.
3. **Don't wrap previews in `SharedTransitionLayout`** unless the preview is specifically for the
   transition; the null-checks handle the absent scope.
4. **Same key on both ends, unique across the app.** The framework only pairs identical keys within a
   single `SharedTransitionScope`.
5. **One `SharedTransitionLayout` per morph.** Two nested `SharedTransitionLayout`s create two
   different scopes that never pair. Provide exactly one and reuse it via `LocalSharedTransitionScope`.
6. **If you use `SaveableStateProvider` with a Decompose child key**, pass a `String` — `Child.key`
   is the `Configuration` instance, which Android can't put in a `Bundle` (runtime crash on first
   navigation, not a compile error). Build a string like `"${configuration::class.simpleName}_${key.hashCode()}"`.

## Verification checklist

1. `./gradlew ktfmtFormat`
2. `./gradlew :client:composeApp:compileKotlinJvm` — must pass.
3. **Run on Android.** A passing JVM compile won't catch the `Bundle` crash above; it appears at
   runtime on first navigation. If you can't run it, say so — don't claim success.
4. Visually verify the morph grows/shrinks smoothly rather than snapping.
5. **Confirm root predictive back still works** — swipe-back from any screen should still show the
   gesture-tracked animation. If it snaps instead, a shared element leaked into the root stack.

## Reference implementations in this repo

- `client/recipe/core/impl/.../detail/ui/RecipeDetailScreen.kt` + `RecipeImageFullScreenScreen.kt` —
  in-screen morph driven by a Decompose `ChildSlot`, local `SharedTransitionLayout`.
- `client/browser/public/.../BrowserRootScreen.kt` — same-stack siblings, scopes via parameters.
- `client/ui/public/.../components/RecipeImage.kt` — opt-in key parameter pattern.
- `client/ui/public/.../SharedTransitionScopes.kt` — CompositionLocals + `sharedElementBy` helpers.
- `client/root/public/.../RootScreen.kt` — **counter-example**: pure `Children` +
  `predictiveBackAnimation`, no shared element. This is what the root must stay.
