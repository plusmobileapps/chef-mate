# `:client:watchShared` — watchOS shared Kotlin framework

The Compose-free Kotlin business-logic layer for the **watchOS companion app**. Compose
Multiplatform has no watchOS target, so the watch UI is native SwiftUI while all data, sync, and
auth are **reused from the existing Kotlin** via this module. It builds a static
`WatchShared.framework` (watchosArm64 + watchosSimulatorArm64) that the SwiftUI watch app links.

## What it exposes

`WatchGroceryController` is the single Swift-facing entry point. Get one from the DI graph:

```kotlin
val controller = WatchApplicationComponent.create().groceryController
```

| Member | Purpose |
|---|---|
| `observeLists(onEach)` / `observeItems(listId, onEach)` | Stream grocery lists / items (returns a `WatchCancellable`). |
| `observeSignedIn(onEach)` | `true` once a Supabase session exists. |
| `ensureDefaultList()` | Local id of the default list (creates one if needed). |
| `addItem(listId, name)` | Add an item (offline-first; syncs later). |
| `setChecked(itemId, isChecked)` | Toggle an item. |
| `syncNow()` | Push local changes + pull remote. |
| `importSession(refreshToken)` | Adopt a Supabase session handed off from the phone. |

Reads are callback-based (Kotlin `Flow` doesn't bridge cleanly to Swift); mutations are `suspend`
(→ Swift `async`, convenient for `AppIntent.perform()`). DTOs (`WatchGroceryList`,
`WatchGroceryItem`) use primitives only, so Swift needs nothing beyond `import WatchShared`.

## How it stays Compose-free

The DI graph (`WatchApplicationComponent`) aggregates the `@ContributesTo`/`@ContributesBinding`
bindings from the data modules on its classpath (SupabaseModule, DatabaseComponent,
CoroutinesComponent, SettingsComponent, grocery/auth repositories) and supplies the three leaf
deps those need but that normally come from Compose-coupled modules absent on watchOS:
`DriverFactory`, `EnvironmentProvider` (fixed to PROD — see `WatchEnvironmentProvider`), and
`DateTimeUtil` (a `Clock`-based `WatchDateTimeUtil`).

## Build / verify

```bash
./gradlew :client:watchShared:linkDebugFrameworkWatchosSimulatorArm64   # build the framework
./gradlew :client:watchShared:jvmTest                                   # facade unit tests
```

## The native watchOS app (`iosApp/ChefMateWatch/`)

The SwiftUI app is wired into `iosApp/iosApp.xcodeproj` (watch target + framework embed via
`scripts/add_watch_target.rb`): a lists screen → items screen (tap to toggle, add sheet), a Siri
`AddGroceryItemIntent`, and a `WatchConnectivityManager`. All state/sync is this module's
`WatchGroceryController` — SwiftUI just renders and forwards taps.

### Session handoff (access-token model)

The watch has no login screen; it receives its session from the iPhone over WatchConnectivity.
To avoid Supabase refresh-token rotation signing the phone out, **only the phone holds the refresh
token**:

- **Phone** (`WatchSessionSender` + `WatchSessionRelay` on the iOS framework) pushes the current
  **access token** + expiry via `updateApplicationContext` on auth changes, and replies to the
  watch's pull requests with a freshly-auto-refreshed token.
- **Watch** (`WatchConnectivityManager`) pulls a token when it becomes active and calls
  `controller.importSession(accessToken, expiresAtEpochSeconds)`, which imports the session with
  `autoRefresh = false` (the watch never refreshes). When the token expires it pulls another.

### Remaining polish

- A real `match` provisioning profile for `…ChefMate.watchkitapp` (currently Automatic signing for
  dev builds) and app icons.
- Optionally push a fresh token from the phone on each of *its own* silent refreshes (today the
  watch pulls on activate, which covers the common case).
