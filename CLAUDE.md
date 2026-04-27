# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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
```

For iOS, open `/iosApp` in Xcode or use the IDE run configuration.

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

### BLoC Pattern (Decompose)

Every screen is a BLoC. The pattern is:

1. **Interface** (`GroceryListBloc`) — declares `state: StateFlow<Model>`, click handlers, nested `Model`, `Output`, and a `Factory` fun interface.
2. **Impl** (`GroceryListBlocImpl`) — annotated with `@Inject` + `@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = ...)`. Delegates `BlocContext by context`. Gets a ViewModel via `instanceKeeper.getViewModel { ... }`.
3. **ViewModel** — annotated `@Inject`. Extends `ViewModel(@Main mainContext)`. Uses `scope` (coroutine scope on main thread) for async work. Holds `MutableStateFlow<State>`.

Navigation BLoCs expose `routerState: Value<ChildStack<*, Child>>`, use `StackNavigation<Configuration>` with `childStack()`, and use serializable `Configuration` sealed classes.

See `docs/architecture.md` for full annotated examples of both patterns.

### Data Layer

- **Offline-first**: All data stored locally via SQLDelight (`client/database/core` module).
- **Remote**: Supabase (Kotlin Multiplatform SDK) for sync.
- Repositories mediate between local cache and remote source.

### UI Layer

- All UI is Compose Multiplatform, shared across all client targets.
- Screen composables use the `Screen` suffix (e.g., `RecipeListScreen.kt`) and live in the feature's `public` module.
- Reusable components live in `client/ui/public`, prefixed with `Plus` (e.g., `PlusHeaderContainer`).

### TextData

Use `TextData` (sealed class) for all display strings to separate domain from UI:
- `FixedString("raw string")` — for API data or previews
- `ResourceString(Res.string.key)` — for localized resources
- `PhraseModel(Res.string.key, "arg" to FixedString("value"))` — for formatted strings

Call `.localized()` inside Composables to resolve to a `String`. Expose `text` module with `api` (not `implementation`) in `public` modules.

### Localized Strings

Add strings at `client/<module>/src/commonMain/composeResources/values/strings.xml`. Rebuild to generate accessors. Import the `compose` plugin and `compose.components.resources` dependency in the module's `build.gradle.kts`.

### Dependency Injection

- `kotlin-inject` + `kotlin-inject-anvil` + `kotlin-inject-anvil-extensions` (assisted factory)
- `AppScope` is the standard scope for binding implementations
- `@ContributesAssistedFactory` binds impl classes to their assisted factory interface
- KSP is set to v1 (not v2) for iOS compatibility — do not change this

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
| Architecture docs | `docs/architecture.md` |
