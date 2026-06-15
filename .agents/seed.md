# Shared Agent Seed

This file provides shared guidance to Claude Code and Codex when working with code in this repository.

## Commands

```bash
# First-time setup (installs ktfmt + pre-commit hook)
./scripts/setup-hooks.sh

# Android
./gradlew :client:composeApp:installDebug

# Desktop (JVM)
./gradlew :client:composeApp:run

# Server
./gradlew :server:run

# Lint / format
./gradlew ktfmtFormat   # auto-format
./gradlew ktfmtCheck    # check only

# Tests (all)
./gradlew test

# Tests (single module)
./gradlew :client:grocery:core:impl:test

# Snapshot tests (record then verify)
./gradlew :client:ui:screenshot-test:updateDebugScreenshotTest
./gradlew :client:ui:screenshot-test:validateDebugScreenshotTest
```

For iOS, open `/iosApp` in Xcode or use the IDE run configuration.

## Git workflow

- **Never `git push` without an explicit request from the user in the current message.** "Commit it" / "save this" / "go ahead" authorize a commit but not a push. The push is always a separate, user-requested action.
- **Never bundle `git push` into the same Bash invocation as `git add` / `git commit`.** Run the commit on its own and stop. The user will issue a separate "push" instruction when they're ready.
- **When opening a PR, split work into multiple commits by concern type so reviewers can step through them.** A typical order: refactors/renames first (no behavior change), then the feature or fix, then tests, then docs / strings / generated artifacts. One concern per commit, each with a conventional-commit subject (`refactor(recipe): …`, `feat(recipe): …`, `test(recipe): …`, `docs: …`). If a change is genuinely one concern, one commit is fine — don't manufacture splits.
- **For feature work, split commits along the testing layers (see Architecture below):**
  1. **One commit per BLoC, bundling that BLoC's unit tests with it.** Each BLoC (interface + impl + ViewModel) ships in the same commit as its `impl/src/commonTest` unit tests. If a PR touches several BLoCs, give each its own commit.
  2. **Snapshots in their own commit.** The `<Feature>Previews.kt` previews, the `screenshot-test` `@PreviewTest` wrappers, and the recorded reference PNGs go together in a single dedicated commit, separate from the BLoC logic.
  3. **Automation tests last.** Robot UI tests and the `impl-robots` modules that back them (the `<Feature>Robot.kt` classes and the `runRootBlocTest { … }` flow tests) come in the final commit.

## Architecture

This is a Kotlin Multiplatform app targeting Android, iOS, Desktop (JVM), Web (WASM), and a Ktor server. All new code must be Kotlin.

### Module Layers

Every feature is split into three module types:

- `public` — API contracts (interfaces, models, UI screens). May depend on other `public` modules only.
- `impl` — Production implementation. Depends on `public` modules only. Every new `impl` module must be added to `client/composeApp/build.gradle.kts` under `commonMain` to register it in the DI graph.
- `testing` — Fake/stub implementations for use in other modules' tests.

The `plusLibrary` extension in each module's `build.gradle.kts` controls convention plugin features:
- `enableDi = true` — sets up Metro (kotlin-inject) dependency injection
- `enableTesting = true` — adds test dependencies (mokkery, turbine, kotest, coroutines-test)
- `enableDatabaseTesting = true` — adds `client/database/testing` (in-memory SQLDelight) to test dependencies and links sqlite3 for iOS. Use this when tests need a real database via `createTestDatabase()`. Requires `enableTesting = true`.
- `uiTest = true` — adds `compose.uiTest` as an `api` dependency on `commonMain`. Used by `impl-robots` modules that expose reusable Compose UI test robots. Requires the `compose` plugin to also be applied.

### BLoC Pattern (Decompose)

Every screen is a BLoC. The pattern is:

1. **Interface** (`GroceryListBloc`) — declares `state: StateFlow<Model>`, click handlers, nested `Model`, `Output`, and a `Factory` fun interface.
2. **Impl** (`GroceryListBlocImpl`) — annotated with `@Inject` + `@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = ...)`. Delegates `BlocContext by context`. Gets a ViewModel via `instanceKeeper.getViewModel { ... }`.
3. **ViewModel** — annotated `@Inject`. Extends `ViewModel(@Main mainContext)`. Uses `scope` (coroutine scope on main thread) for async work. Holds `MutableStateFlow<State>`.

Navigation BLoCs expose `routerState: Value<ChildStack<*, Child>>`, use `StackNavigation<Configuration>` with `childStack()`, and use serializable `Configuration` sealed classes. The `Child` sealed class exposes an `abstract val bloc: BlocScreen`, and each variant overrides it with its concrete bloc (`data class Detail(override val bloc: RecipeDetailBloc) : Child()`). This lets the navigation screen render any child uniformly with `child.instance.bloc.Content()` — no `when` over child types.

See `docs/architecture.md` for full annotated examples of both patterns.

### Data Layer

- **Offline-first**: All data stored locally via SQLDelight (`client/database/core` module).
- **Remote**: Supabase (Kotlin Multiplatform SDK) for sync.
- Repositories mediate between local cache and remote source.

### UI Layer

- All UI is Compose Multiplatform, shared across all client targets.
- Screen composables use the `Screen` suffix (e.g., `RecipeListScreen.kt`) and live in the feature's `public` module.
- Reusable components live in `client/ui/public`, prefixed with `Plus` (e.g., `PlusHeaderContainer`).
- **Prefer a shared `Plus*` component over a raw Material composable when one exists.** For example, use `PlusDialog` instead of Material `AlertDialog`. Check `client/ui/public/.../components` before reaching for a Material primitive.
- **Spacing/sizing dimensions should come from the theme (`ChefMateTheme.dimens`), not hardcoded `.dp` literals.** Use the closest `AppDimensions` token (`paddingExtraSmall` 4, `paddingSmall` 8, `paddingNormal` 16, `paddingLarge` 24, `paddingExtraLarge` 32, `rowHeight` 56, `fabClearance` 88). Only fall back to a raw `.dp` literal when the value is genuinely off-spec (no matching token); if an off-spec value recurs, add a token to `AppDimensions` instead.

### Navigation Animations & Shared Elements

- Navigation (root and per-feature) renders a Decompose `ChildStack` with `Children` + `predictiveBackAnimation`. Use the shared `backAnimation` helper in `client/ui/public/.../BackAnimation.kt` (a `predictiveBackAnimation` with a `slide` fallback). `RootScreen` inlines `predictiveBackAnimation` directly because it varies the fallback by child type (modal screens slide vertically). **Predictive back is the priority** — the system back gesture/animation must keep working across the whole stack.
- **Never wrap root navigation in `SharedTransitionLayout` + `AnimatedContent`.** Doing so replaces Decompose's `Children` and disables predictive back — that combination was removed for exactly this reason.
- Shared-element transitions are only for **self-contained, in-screen morphs** that own their own navigation and do not cross the root stack. Two examples exist: the recipe-detail full-screen image (`RecipeDetailScreen` wraps its *own* `SharedTransitionLayout` around an inner `AnimatedContent`) and the browser address bar (`BrowserRootScreen`). Each provides its scope locally, so predictive back elsewhere is unaffected.
- To add a shared element: wrap the owning screen in a local `SharedTransitionLayout`, provide `LocalSharedTransitionScope` from it, and use the `sharedElementBy` helpers + the `Local*VisibilityScope` composition locals in `client/ui/public/.../SharedTransitionScopes.kt`. Do **not** hoist the scope to the root.

### TextData

Use `TextData` (sealed class) for all display strings to separate domain from UI:
- `FixedString("raw string")` — for API data or previews
- `ResourceString(Res.string.key)` — for localized resources
- `PhraseModel(Res.string.key, "arg" to FixedString("value"))` — for formatted strings

Call `.localized()` inside Composables to resolve to a `String`. Expose `text` module with `api` (not `implementation`) in `public` modules.

### Localized Strings

Add strings at `client/<module>/src/commonMain/composeResources/values/strings.xml`. Rebuild to generate accessors. Import the `compose` plugin and `compose.components.resources` dependency in the module's `build.gradle.kts`.

**Apostrophes and quotes.** Use the typographic curly glyphs (`’`, `‘`, `“`, `”`) directly in `strings.xml` — not ASCII straight quotes (`'`, `"`). The codebase convention is curly throughout (e.g., `auth_switch_to_sign_up: "Don’t have an account?"`), and curly glyphs don't need the XML backslash escape that ASCII apostrophes require (`You\'re` → `You’re`).

### Dependency Injection

- `dev.zacsweers.metro` is the DI framework (compiler plugin). `AppScope` is the standard scope.
- `@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = FooBloc.Factory::class)` from `com.plusmobileapps.metro-extensions` is placed alongside `@AssistedInject` on each BLoC impl. A KSP processor generates the Metro `@AssistedFactory` and a `@ContributesBinding`/`@Origin`-tagged bridge to the public `Factory` interface — no manual binding modules needed.
- KSP + metro-extensions are wired automatically by `MetroConventionPlugin` for any module with `plusLibrary { enableDi = true }`.

### Snapshot Testing

**Rule: any change that touches a screen, composable, or shared UI component must add or update a snapshot test in the same commit/PR.** If a `<Feature>ScreenshotTest.kt` already covers the affected preview, refresh the reference with `./gradlew :client:ui:screenshot-test:updateDebugScreenshotTest` and visually inspect the diff before committing. If no snapshot exists for the screen yet, add one following the pattern below — including dark-mode and any meaningful state variants (empty, loading, error). CI only catches regressions of *existing* references, so missing coverage on new UI won't fail the build.

Compose preview screenshot tests live in **`client/ui/screenshot-test`** — a plain Android library module (not KMP) that uses Google's `com.android.compose.screenshot` plugin. KMP modules can't host the `screenshotTest` source set with `com.android.library`, so all snapshot tests are centralized here and depend on the relevant feature `public` modules.

**Pattern:**
1. In the feature's `public` module, add a `<Feature>Previews.kt` file in `commonMain` with:
   - **public** `previewXxxBloc` `val`s — fake Bloc implementations (`object : XxxBloc { ... }` returning `MutableStateFlow(model)` and `Unit` for handlers). Public visibility is required so `screenshot-test` can reuse them.
   - `internal @Preview @Composable` functions for the IDE preview pane.
2. In `client/ui/screenshot-test/build.gradle.kts`, add `implementation(project(":client:<feature>:public"))`.
3. In `client/ui/screenshot-test/src/screenshotTest/kotlin/.../<Feature>ScreenshotTest.kt`, write `@PreviewTest @Preview @Composable` wrappers that import the public preview Blocs and call the screen.
4. Record references: `./gradlew :client:ui:screenshot-test:updateDebugScreenshotTest`. Visually inspect the PNGs under `client/ui/screenshot-test/src/screenshotTestDebug/reference/` before committing.
5. CI runs `validateDebugScreenshotTest` via the `screenshot-tests` job in `.github/workflows/tests.yml`. Diff reports upload as artifacts on failure.

**Reusable fixtures**: `Recipe.Sample` is a populated companion val on `Recipe` (next to `Recipe.Empty`) for any preview that needs realistic recipe data. Add similar `<Type>.Sample` companions on shared data classes rather than redefining them per feature.

**Caveats**: Plugin is Android-only (no JVM/iOS targets) and currently alpha (`com.android.compose.screenshot:0.0.1-alpha14`). `@PreviewTest` annotations only go in `screenshot-test` — never in production modules, since the annotation lives in a test-only artifact.

See `client/cook/public/.../CookModePreviews.kt` and `client/ui/screenshot-test/.../CookModeScreenshotTest.kt` for a worked example covering stacked, split, loading, empty, and dark variants.

### Robot UI Tests

**Rule: every new feature must ship with a multiplatform Compose UI test using the `impl-robots` pattern.** Robots wrap `ComposeUiTest` with domain-level vocabulary so test cases read as user flows, not semantics-tree lookups. They run on all client targets (Android, iOS, Desktop, Web) because they live in `commonMain`.

**Pattern:**
1. For a new feature `client/<feature>/<sub>/impl`, create a sibling module `client/<feature>/<sub>/impl-robots` with a `build.gradle.kts` that applies `kmpLibrary` + `compose`, sets `plusLibrary { uiTest = true }` (which wires the `compose.uiTest` `api` dependency), and adds `implementation(projects.client.<feature>.<sub>.public)`. See `client/recipe/list/impl-robots/build.gradle.kts` for the minimal shape.
2. Add `<Feature>Robot.kt` under `src/commonMain/kotlin/.../<feature>/robots/`. Take `ComposeUiTest` in the constructor. Scope every node lookup to a descendant of the screen's root test tag (`hasAnyAncestor(hasTestTag(<Feature>TestTags.SCREEN))`) so titles rendered on other screens don't satisfy matchers. Provide a factory extension (e.g. `fun ComposeUiTest.recipeList() = …`). Return `<Feature>Robot` from every action method for chaining.
3. Add a test in `client/composeApp/src/commonTest/...` (or the feature's own test module) that runs through `runRootBlocTest { … }` and composes one or more robots into a user flow. Reference example: `client/composeApp/src/commonTest/.../RootNavigationUiTest.kt`.
4. For async UI states, expose an `awaitDisplayed()` on the robot using `waitUntilExactlyOneExists` — see `client/recipe/core/impl-robots/.../RecipeDetailRobot.kt`.

Robots are *not* unit tests of a BLoC — those still live in `impl/src/commonTest`. Robots cover the rendered screen and cross-screen navigation flows.

### Key Paths

| Concern | Path |
|---|---|
| App entry (common) | `client/composeApp/src/commonMain/.../App.kt` |
| DI component | `client/composeApp/src/commonMain/.../ApplicationComponent.kt` |
| Database schemas | `client/database/core/src/commonMain/sqldelight/` |
| Root navigation | `client/root/` |
| Shared utilities | `client/shared/` |
| Reusable UI | `client/ui/public/` |
| TextData | `client/text/public/` |
| Snapshot tests | `client/ui/screenshot-test/` |
| Robot UI tests | `client/<feature>/impl-robots/` |
| Architecture docs | `docs/architecture.md` |
