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

## Remaining work — native watchOS app (needs Xcode)

The following live in `iosApp/` and require Xcode; they are **not** in this module/PR:

1. **Add a watchOS App target** to `iosApp/iosApp.xcodeproj` (SwiftUI lifecycle). Bundle id
   `com.plusmobileapps.chefmate.ChefMate.watchkitapp`; deployment target watchOS 10+.
2. **Embed `WatchShared.framework`** via a "Run Script" build phase that invokes the KMP
   `embedAndSignAppleFrameworkForXcode` task for `:client:watchShared` — clone the existing
   `ComposeApp` framework build phase on the iOS app target.
3. **SwiftUI UI**: a lists screen (`observeLists`) → items screen (`observeItems`, tap = `setChecked`,
   "+" = dictation/scribble → `addItem`); call `syncNow()` on scene-active.
4. **WatchConnectivity**: iOS side pushes `{refreshToken}` via `updateApplicationContext` when the
   Supabase session changes; watch side calls `controller.importSession(refreshToken)`.
5. **Siri App Intents**: `AddGroceryItemIntent` (+ `AppShortcutsProvider`) whose `perform()` calls
   `controller.addItem(...)` then `syncNow()`. Add to the watch + phone targets.
