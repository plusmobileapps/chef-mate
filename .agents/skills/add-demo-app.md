# add-demo-app

Stand up a standalone **demo app** for a single feature — e.g. `:client:onboarding:demo` — so the
feature can be built and run on its own (Android + JVM desktop) without compiling the whole
`:client:composeApp`. The demo renders the feature's **real** screens/BLoCs, driven by a tiny
per-demo Metro graph; heavy data-layer dependencies are swapped for fakes. Goal: minimal
dependencies, fast iteration, visual review.

## Required input

Ask the user (if not provided):

1. **Which feature** — e.g. `onboarding`. The demo module path is `client/<feature>/demo`.
2. **Which entry screen / BLoC** — the screen to showcase. Prefer a feature's **root** navigation
   BLoC if it has one (e.g. `OnboardingRootBloc`, `SettingsRootBloc`) — it already drives the whole
   flow, so the demo gets multi-screen navigation for free. Otherwise pick the single screen's BLoC.

## Where the pieces live

| File | Purpose |
|---|---|
| `client/<feature>/demo/build.gradle.kts` | Applies `plusApplication`; declares the minimal dep set + `compose.desktop` mainClass. |
| `commonMain/.../<feature>/demo/<Feature>DemoComponent.kt` | Metro `@DependencyGraph(AppScope::class)` exposing the BLoC factory + any `@Provides` fakes. |
| `commonMain/.../<feature>/demo/<Feature>DemoApp.kt` | `buildXxxDemoBloc(componentContext)` helper + `@Composable XxxDemoApp(bloc)` (wraps `bloc.Content()` in `ChefMateTheme`). |
| `androidMain/.../<feature>/demo/MainActivity.kt` | `ComponentActivity` → `defaultComponentContext()` → `setContent { XxxDemoApp(bloc) }`. |
| `androidMain/AndroidManifest.xml` | Single exported launcher activity (MAIN/LAUNCHER only). |
| `jvmMain/.../<feature>/demo/main.kt` | `fun main()` with `LifecycleRegistry` + `BackDispatcher` + `DefaultComponentContext`, `application { Window { XxxDemoApp(bloc) } }`. |
| `settings.gradle.kts` | `include(":client:<feature>:demo")`. |

The `plusApplication` convention plugin (`build-logic/.../convention/PlusApplicationConventionPlugin.kt`)
applies Kotlin Multiplatform + `com.android.application` + the shared `compose` convention + ktfmt,
configures the `androidTarget()` + `jvm()` targets and the AGP application block, and (by default,
`enableDi = true`) the `metro` convention. The reference implementation is `client/onboarding/demo`
— copy it.

## The pattern

### 1. `build.gradle.kts`

```kotlin
plugins { alias(libs.plugins.plusApplication) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.client.<feature>.impl)
            implementation(projects.client.<feature>.public)
            implementation(projects.client.shared)    // AppScope + @Main (CoroutinesComponent)
            implementation(projects.client.ui.public)  // ChefMateTheme + ComposeScreen
            // + one public/testing pair per leaf dependency the BLoC's ViewModel needs
            implementation(libs.arkivanov.decompose.core)
            implementation(libs.arkivanov.decompose.compose.extensions)
        }
    }
}

plusApplication { namespace = "com.plusmobileapps.chefmate.<feature>.demo" }

compose.desktop {
    application { mainClass = "com.plusmobileapps.chefmate.<feature>.demo.MainKt" }
}
```

### 2. Map the BLoC's dependencies

Read the BLoC impl + its ViewModel constructor. For each non-`@Assisted` parameter:

- **`@Main`/`@IO`/`@CPU` `CoroutineContext`** — free; `client/shared`'s `CoroutinesComponent` is
  `@ContributesTo(AppScope::class)` and is on the classpath via `projects.client.shared`.
- **A simple `@Inject` class whose own deps are satisfiable** (e.g. `OnboardingRepository`, which
  just needs the contributed `Settings`) — nothing to do; Metro constructs it.
- **A repository/service interface with a `testing` fake** — depend on its `public` + `testing`
  modules and `@Provides` the fake in the demo graph.
- **A use case / binding whose `impl` drags in the data layer** (Supabase, SQLDelight, many
  repositories) — do **not** depend on the `impl`. Provide a lightweight stub directly:
  `@Provides fun signOut(): SignOutUseCase = SignOutUseCase {}` (works for `fun interface`s).

Only the dependencies **reachable from the BLoC factory you expose** must be satisfied — Metro
validates lazily, so other BLoCs contributed by the same `impl` module won't force their deps.

### 3. `<Feature>DemoComponent.kt`

A self-contained feature needs no `@Provides` (onboarding):

```kotlin
@DependencyGraph(AppScope::class)
@SingleIn(AppScope::class)
interface OnboardingDemoComponent {
    val onboardingRootBlocFactory: OnboardingRootBloc.Factory   // auto-contributed via @ContributesAssistedFactory

    @DependencyGraph.Factory
    fun interface Factory { fun create(): OnboardingDemoComponent }
}
```

When the BLoC has heavy leaf deps, add fakes:

```kotlin
@Provides @SingleIn(AppScope::class)
fun authRepo(): AuthenticationRepository = FakeAuthenticationRepository().apply { setAuthenticated() }
```

### 4. Shared app + entrypoints

`<Feature>DemoApp.kt` (commonMain):

```kotlin
fun buildOnboardingDemoBloc(componentContext: ComponentContext): OnboardingRootBloc {
    val component = createGraphFactory<OnboardingDemoComponent.Factory>().create()
    return component.onboardingRootBlocFactory.create(
        context = DefaultBlocContext(componentContext = componentContext),
        output = Consumer { output -> println("OnboardingDemo bloc output: $output") },
    )
}

@Composable
fun OnboardingDemoApp(bloc: OnboardingRootBloc, modifier: Modifier = Modifier) {
    ChefMateTheme { bloc.Content(modifier) }
}
```

`MainActivity.kt` (androidMain) builds the bloc with `defaultComponentContext()`; `main.kt`
(jvmMain) mirrors `client/composeApp/src/jvmMain/.../main.kt` with a `LifecycleRegistry` +
`BackDispatcher` + `DefaultComponentContext` inside the `application { Window { … } }` block. Copy
both from `client/onboarding/demo`.

### 5. Register + build loop

1. Add `include(":client:<feature>:demo")` to `settings.gradle.kts`.
2. `./gradlew :client:<feature>:demo:assembleDebug` (Android compile-check, no device) and
   `./gradlew :client:<feature>:demo:compileKotlinJvm` (desktop).
3. For each binding Metro reports missing, add the matching `public` + `testing` (fake) module or
   a `@Provides` stub — keep additions to `public`/`testing`, never a heavy `impl`.
4. `./gradlew :client:<feature>:demo:ktfmtFormat`.
5. Run it: `:demo:run` (desktop) or `:demo:installDebug` (Android emulator/device).

## Caveats

- **Don't add the demo to `client/composeApp`.** It's standalone — that's the whole point.
- **No iOS target.** Demos are Android + JVM only.
- The demo's `androidMain` source set requires an Android SDK; a worktree needs its own
  `local.properties` (`sdk.dir=…`) — it's gitignored.
